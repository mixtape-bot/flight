package me.devoxin.flight.internal.utils

import me.devoxin.flight.api.annotations.Name
import me.devoxin.flight.api.annotations.RateLimit
import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.message.MessageSubCommandFunction
import me.devoxin.flight.api.command.message.annotations.*
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

object MessageCommandUtil {
    private val logger = LoggerFactory.getLogger(MessageCommandUtil::class.java)

    fun loadCommand(meth: KFunction<*>, cog: Cog): MessageCommandFunction {
        val name = meth.findAnnotation<Name>()?.name
            ?: meth.name

        check(meth.javaMethod!!.declaringClass == cog::class.java) {
            "$name is not from ${cog::class.simpleName}"
        }

        require(meth.hasAnnotation<MessageCommand>()) {
            "$name is not annotated with Command!"
        }

        val category = cog.name()
            ?: cog::class.java.`package`.name.split('.')
                .last()
                .replace('_', ' ')
                .lowercase()
                .replaceFirstChar { it.uppercase() }

        val properties = meth.findAnnotation<MessageCommand>()!!
        val rateLimit = meth.findAnnotation<RateLimit>()
        val ctxParam = meth.valueParameters.firstOrNull {
            it.type.classifier?.equals(Context::class) == true
        }

        require(ctxParam != null) {
            "${meth.name} is missing the Context parameter!"
        }

        val parameters = meth.valueParameters.filterNot {
            it.type.classifier?.equals(Context::class) == true
        }

        val arguments = loadParameters(parameters)
        val subcommands = getSubCommands(cog)
        val cogParentCommands = cog::class.functions.filter {
            it.hasAnnotation<MessageCommand>()
        }

        if (subcommands.isNotEmpty() && cogParentCommands.size > 1) {
            throw IllegalStateException("SubCommands are present within ${cog::class.simpleName} however there are multiple top-level commands!")
        }

        return MessageCommandFunction(
            name,
            meth,
            cog,
            arguments,
            ctxParam,
            category,
            properties,
            rateLimit,
            subcommands
        )
    }

    fun getSubCommands(cog: Cog): List<MessageSubCommandFunction> {
        logger.debug("Scanning ${cog::class.simpleName} for sub-commands...")

        val cogClass = cog::class
        val subcommands = cogClass.members
            .filterIsInstance<KFunction<*>>()
            .filter { it.hasAnnotation<MessageSubCommand>() }
            .map { loadSubCommand(it, cog) }

        logger.debug("Found ${subcommands.size} sub-commands in cog ${cogClass.simpleName}")
        return subcommands.toList()
    }

    private fun loadSubCommand(meth: KFunction<*>, cog: Cog): MessageSubCommandFunction {
        /* get the name of this sub-command */
        val name = meth.findAnnotation<Name>()?.name
            ?: meth.name.lowercase()

        /* do some checks */
        check(meth.javaMethod!!.declaringClass == cog::class.java) {
            "${meth.name} is not from ${cog::class.simpleName}"
        }

        require(meth.hasAnnotation<MessageSubCommand>()) {
            "${meth.name} is not annotated with MessageSubCommand!"
        }

        /* load properties */
        val properties = meth.findAnnotation<MessageSubCommand>()!!

        /* find the message context parameter. */
        val ctxParam = meth.valueParameters.firstOrNull {
            it.type.classifier?.equals(Context::class) == true
        }

        require(ctxParam != null) {
            "${meth.name} is missing the MessageContext parameter!"
        }

        /* find arguments */
        val parameters = meth.valueParameters.filterNot {
            it.type.classifier?.equals(Context::class) == true
        }

        val arguments = loadParameters(parameters)

        /* return sub command function */
        return MessageSubCommandFunction(name, properties, meth, cog, arguments, ctxParam)
    }

    private fun loadParameters(parameters: List<KParameter>): List<CommandArgument> {
        val arguments = mutableListOf<CommandArgument>()

        for (p in parameters) {
            val pName = p.findAnnotation<Name>()?.name ?: p.name ?: p.index.toString()
            var type = p.type.jvmErasure.javaObjectType
            var greedy = p.findAnnotation<Greedy>()?.let { GreedyInfo(it.type, it.min..it.max) }
            val isOptional = p.isOptional
            val isNullable = p.type.isMarkedNullable
            val isTentative = p.hasAnnotation<Tentative>()

            if (p.type.jvmErasure.isSubclassOf(Collection::class)) {
                type = p.type.arguments.first().type!!.jvmErasure.javaObjectType
                greedy = GreedyInfo(GreedyType.Regular, greedy?.range ?: 1..Int.MAX_VALUE)
            } else if (greedy != null) {
                greedy = GreedyInfo(GreedyType.Computed, greedy.range)
            }

            if (isTentative && !(isNullable || isOptional)) {
                throw IllegalStateException("${p.name} is marked as tentative, but does not have a default value and is not marked nullable!")
            }

            arguments.add(CommandArgument(pName, type, greedy, isOptional, isNullable, isTentative, null, p))
        }

        return arguments
    }
}
