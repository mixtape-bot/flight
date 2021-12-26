package me.devoxin.flight.internal.arguments

import arrow.core.Option
import arrow.core.computations.ResultEffect.bind
import arrow.core.none
import arrow.core.toOption
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.annotations.Greedy.Companion.range
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.exceptions.ResolverNotRegistered
import me.devoxin.flight.internal.entities.Executable
import me.devoxin.flight.internal.arguments.resolvers.Resolver
import me.devoxin.flight.internal.utils.splice
import me.devoxin.flight.internal.utils.unwrap
import java.util.HashMap
import kotlin.reflect.KParameter

public class ArgParser(private val ctx: Context, private val delimiter: Char, args: List<String>) {
    public companion object {
        public val resolvers: HashMap<Class<*>, Resolver<*>> = hashMapOf()

        public fun <T> useResolver(javaClass: Class<T>, resolver: Resolver<T>): Companion {
            resolvers[javaClass] = resolver
            return this
        }

        public inline fun <reified T : Any> useResolver(resolver: Resolver<T>): Companion =
            useResolver(T::class.java, resolver)

        public suspend fun parseArguments(ctx: Context, cmd: Executable, delimiter: Char): MutableMap<KParameter, Any?> {
            if (cmd.arguments.isEmpty()) {
                return mutableMapOf()
            }

            val args = ctx.args.takeUnless { delimiter == ' ' } ?: ctx.args
                .joinToString(" ")
                .split(delimiter)
                .toMutableList()

            val parser = ArgParser(ctx, delimiter, args)
            val resolvedArgs = mutableMapOf<KParameter, Any?>()

            for (arg in cmd.arguments) {
                val res = parser.resolve(arg)
                val useValue = res != null || (arg.isNullable && !arg.optional) || (arg.isTentative && arg.isNullable)

                if (useValue) {
                    /* This will only place the argument into the map if the value is null,
                       or if the parameter requires a value (i.e. marked nullable). */

                    /* Commands marked optional already have a parameter, so they don't need user-provided values
                       unless the argument was successfully resolved for that parameter. */

                    resolvedArgs[arg.kparam] = res
                }
            }

            return resolvedArgs
        }
    }

    private val delimiterStr = delimiter.toString()
    private var args = args.toMutableList()

    private fun take(amount: Int) = args.splice(amount)

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

                // accept other quote chars
                quoting && char == '"' -> quoting = false

                // accept other quote chars
                !quoting && char == '"' -> quoting = true

                // Maybe this should throw? !test  blah -- Extraneous whitespace is ignored.
                !quoting && char == delimiter -> if (argument.isEmpty()) continue@loop else break@loop

                else -> argument.append(char)
            }
        }

        argument.append('"')

        args = buildString { iterator.forEachRemaining(::append) }
            .split(delimiter)
            .toMutableList()

        return argument.toString() to original.split(delimiterStr)
    }

    /**
     * @returns a Pair of the parsed argument, and the original args.
     */
    private fun getNextArgument(greedy: Boolean): Pair<String, List<String>> {
        val (argument, original) = when {
            args.isEmpty() ->
                "" to emptyList()

            greedy -> {
                val args = take(args.size)
                args.joinToString(delimiterStr) to args
            }

            args[0].startsWith('"') && delimiter == ' ' ->
                parseQuoted() // accept other quote chars

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

    public suspend fun resolve(arg: Argument): Any? {
        val resolver = resolvers[arg.type] as? Resolver<Any>
            ?: throw ResolverNotRegistered("No resolvers registered for `${arg.type}`")

        return when (val type = arg.greedy?.type) {
            Greedy.Type.Normal -> {
                val took = mutableListOf<String>()

                val args = mutableListOf<Any>()
                while (this.args.isNotEmpty()) {
                    val (param, raw) = getNextArgument(false)

                    /* resolve the argument. */
                    val resolved = if (param.isEmpty()) none() else resolver
                        .resolveCatching(ctx, param, arg)
                        .bind()

                    if (resolved.isEmpty()) {
                        if (arg.isTentative) {
                            restore(raw)
                        }

                        break
                    }

                    took.add(raw.joinToString(delimiterStr))
                    args.add(resolved.unwrap())
                }

                val canSubstitute = arg.isNullable || arg.optional
                if (!canSubstitute && args.size !in arg.greedy.range) {
                    val argument = took.joinToString(delimiterStr)
                    throw BadArgument(arg, argument, IllegalArgumentException("Not enough arguments"))
                }

                if (arg.isNullable && args.isEmpty()) null else args
            }

            else -> {
                val (param, raw) = getNextArgument(type == Greedy.Type.Computed)
                val resolved = if (param.isEmpty()) none() else resolver
                    .resolveCatching(ctx, param, arg)
                    .bind()

                resolve(arg, param to raw, resolved)
            }
        }
    }

    private fun resolve(arg: Argument, argument: Pair<String, List<String>>, value: Option<Any>): Any? {
        val canSubstitute = arg.isTentative || arg.isNullable || (arg.optional && argument.first.isEmpty())
        if (value.isEmpty() && !canSubstitute) {
            // canSubstitute -> Whether we can pass null or the default value.
            // This should throw if the result is not present, and one of the following is not true:
            // - The arg is marked tentative (isTentative)
            // - The arg can use null (isNullable)
            // - The arg has a default (isOptional) and no value was specified for it (argument.isEmpty())

            //!arg.isNullable && (!arg.optional || argument.isNotEmpty())) {
            throw BadArgument(arg, argument.first)
        }

        if (value.isEmpty() && arg.isTentative) {
            restore(argument.second)
        }

        return value.orNull()
    }
}
