package me.devoxin.flight.internal.arguments

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.GreedyType
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.exceptions.ParserNotRegistered
import me.devoxin.flight.internal.entities.Executable
import me.devoxin.flight.internal.parsers.Parser
import java.util.*
import kotlin.reflect.KParameter

class ArgParser(
    private val ctx: Context,
    private val delimiter: Char,
    commandArgs: List<String>
) {
    private val delimiterStr = delimiter.toString()
    private var args = commandArgs.toMutableList()

    private fun take(amount: Int) = args.take(amount).onEach { args.removeAt(0) }
    private fun restore(argList: List<String>) = args.addAll(0, argList)

    private fun parseQuoted(): Pair<String, List<String>> {
        val iterator = args.joinToString(delimiterStr).iterator()
        val original = StringBuilder()
        val argument = StringBuilder("\"")
        var quoting = false
        var escaping = false

        loop@ while (iterator.hasNext()) {
            val char = iterator.nextChar()
            original.append(char)

            when {
                escaping -> {
                    argument.append(char)
                    escaping = false
                }
                char == '\\' -> escaping = true
                quoting && char == '"' -> quoting = false // accept other quote chars
                !quoting && char == '"' -> quoting = true // accept other quote chars
                !quoting && char == delimiter -> {
                    // Maybe this should throw? !test  blah -- Extraneous whitespace is ignored.
                    if (argument.isEmpty()) continue@loop
                    else break@loop
                }
                else -> argument.append(char)
            }
        }

        argument.append('"')

        val remainingArgs = StringBuilder().apply {
            iterator.forEachRemaining { this.append(it) }
        }
        args = remainingArgs.toString().split(delimiter).toMutableList()
        return argument.toString() to original.split(delimiterStr)
    }

    /**
     * @returns a Pair of the parsed argument, and the original args.
     */
    private fun getNextArgument(greedy: Boolean): Pair<String, List<String>> {
        val (argument, original) = when {
            args.isEmpty() -> "" to emptyList()
            greedy -> {
                val args = take(args.size)
                args.joinToString(delimiterStr) to args
            }
            args[0].startsWith('"') && delimiter == ' ' -> parseQuoted() // accept other quote chars
            else -> {
                val taken = take(1)
                taken.joinToString(delimiterStr) to taken
            }
        }

        var unquoted = argument.trim()
        if (!greedy) {
            unquoted = unquoted.removeSurrounding("\"")
        }

        return unquoted to original
    }

    suspend fun parse(arg: Argument): Any? {
        val parser = parsers[arg.type]
            ?: throw ParserNotRegistered("No parsers registered for `${arg.type}`")

        var result: Any? = null
        if (arg.greedy != null) {
            when (arg.greedy.type) {
                GreedyType.Regular -> {
                    val resolved = mutableListOf<Any?>()
                    val took = mutableListOf<String>()
                    while (args.isNotEmpty()) {
                        val (argument, original) = getNextArgument(false)
                        val parsed = if (argument.isEmpty()) Optional.empty() else argument
                            .runCatching { parser.parse(ctx, argument) }
                            .getOrElse { throw BadArgument(arg, argument, it) }

                        if (!parsed.isPresent) {
                            if (arg.isTentative) {
                                restore(original)
                            }

                            break
                        }

                        took.add(original.joinToString(delimiterStr))
                        resolved.add(parsed.orElse(null))
                    }

                    val canSubstitute = arg.isNullable || arg.optional
                    if (!canSubstitute && resolved.size !in arg.greedy.range) {
                        val argument = took.joinToString(delimiterStr)
                        throw BadArgument(arg, argument, IllegalArgumentException("Not enough arguments"))
                    }

                    result = if (arg.isNullable && resolved.isEmpty()) null else resolved
                }

                GreedyType.Computed -> {
                    val (argument, original) = getNextArgument(true)
                    val resolved = (if (argument.isEmpty()) Optional.empty() else argument
                        .runCatching { parser.parse(ctx, argument) }
                        .getOrElse { throw BadArgument(arg, argument, it) })
                        .orElse(null)

                    result = parse(arg, argument to original, resolved)
                }
            }
        } else {
            val (argument, original) = getNextArgument(false)
            val resolved = (if (argument.isEmpty()) Optional.empty() else argument
                .runCatching { parser.parse(ctx, argument) }
                .getOrElse { throw BadArgument(arg, argument, it) })
                .orElse(null)

            result = parse(arg, argument to original, resolved)
        }

        return result
    }

    private fun parse(arg: Argument, argument: Pair<String, List<String>>, value: Any?): Any? {
        val canSubstitute = arg.isTentative || arg.isNullable || (arg.optional && argument.first.isEmpty())
        if (value == null && !canSubstitute) {
            // canSubstitute -> Whether we can pass null or the default value.
            // This should throw if the result is not present, and one of the following is not true:
            // - The arg is marked tentative (isTentative)
            // - The arg can use null (isNullable)
            // - The arg has a default (isOptional) and no value was specified for it (argument.isEmpty())

            //!arg.isNullable && (!arg.optional || argument.isNotEmpty())) {
            throw BadArgument(arg, argument.first)
        }

        if (value == null && arg.isTentative) {
            restore(argument.second)
        }

        return value
    }

    companion object {
        val parsers = hashMapOf<Class<*>, Parser<*>>()

        suspend fun parseArguments(
            cmd: Executable,
            ctx: Context,
            delimiter: Char
        ): HashMap<KParameter, Any?> {
            if (cmd.arguments.isEmpty()) {
                return hashMapOf()
            }

            val args = ctx.args.takeUnless { delimiter == ' ' } ?: ctx.args
                .joinToString(" ")
                .split(delimiter)
                .toMutableList()

            val parser = ArgParser(ctx, delimiter, args)

            val resolvedArgs = hashMapOf<KParameter, Any?>()
            for (arg in cmd.arguments) {
                val res = parser.parse(arg)
                val useValue = res != null || (arg.isNullable && !arg.optional) || (arg.isTentative && arg.isNullable)

                if (useValue) {
                    //This will only place the argument into the map if the value is null,
                    // or if the parameter requires a value (i.e. marked nullable).
                    //Commands marked optional already have a parameter so they don't need user-provided values
                    // unless the argument was successfully resolved for that parameter.
                    resolvedArgs[arg.kparam] = res
                }
            }

            return resolvedArgs
        }
    }
}
