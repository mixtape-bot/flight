package me.devoxin.flight.internal.utils

import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.annotations.Name
import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.slash.SlashCommandFunction
import me.devoxin.flight.api.command.slash.SlashContext
import me.devoxin.flight.api.command.slash.SlashSubCommandFunction
import me.devoxin.flight.api.command.slash.SubCommandGroup
import me.devoxin.flight.api.command.slash.annotations.Description
import me.devoxin.flight.api.command.slash.annotations.SlashCommand
import me.devoxin.flight.api.command.slash.annotations.SlashSubCommand
import me.devoxin.flight.api.command.slash.annotations.SubCommandHolder
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import me.devoxin.flight.internal.entities.ICommand
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

// TODO: make this wayyy fucking better, i might as well just remove all of the message command stuff.

@OptIn(FlightPreview::class)
object SlashCommandUtil {
    private val logger = LoggerFactory.getLogger(SlashCommandUtil::class.java)

    fun loadCog(cog: Cog): List<SlashCommandFunction> {
        val holder = cog::class.findAnnotation<SlashCommand>()
        if (holder == null) {
            logger.debug("Scanning ${cog::class.simpleName} for slash commands...")

            val cogClass = cog::class
            val commands = cogClass.members
                .filterIsInstance<KFunction<*>>()
                .filter { it.hasAnnotation<SlashCommand>() }

            logger.debug("Found ${commands.size} slash commands in cog ${cog::class.simpleName}")
            return commands.map { loadICommand(it, cog) as SlashCommandFunction }
        }


        val category = cog.name()
            ?: cog::class.java.`package`.name.split('.')
                .last()
                .replace('_', ' ')
                .lowercase()
                .replaceFirstChar { it.uppercase() }

        val subCommands = loadSubCommands(cog)
        val subCommandGroups = loadSubCommandGroups(cog).associateBy { it.name }

        return listOf(SlashCommandFunction(holder.name, null, cog, emptyList(), null, category, holder, subCommands, subCommandGroups))
    }

    fun loadSubCommandGroups(cog: Cog): List<SubCommandGroup> {
        logger.debug("Scanning ${cog::class.simpleName} for slash sub-command groups...")

        val groups = mutableListOf<SubCommandGroup>()
        for (nested in cog::class.nestedClasses) {
            val holder = nested.findAnnotation<SubCommandHolder>()
                ?: continue

            val subCommands = loadSubCommands(cog, nested.createInstance())
            groups.add(SubCommandGroup(holder.name, holder.description, subCommands))
        }

        return groups
    }

    fun loadSubCommands(cog: Cog, declaringClass: Any = cog): List<SlashSubCommandFunction> {
        logger.debug("Scanning ${declaringClass::class.simpleName} for slash sub-commands...")

        val subCommands = declaringClass::class.members
            .filterIsInstance<KFunction<*>>()
            .filter { it.hasAnnotation<SlashSubCommand>() }

        return subCommands.map { loadICommand(it, cog, declaringClass) as SlashSubCommandFunction }
    }

    fun loadICommand(meth: KFunction<*>, cog: Cog, declaringClass: Any = cog): ICommand {
        /* get the name of this sub-command */
        val name = meth.findAnnotation<Name>()?.name
            ?: meth.name.lowercase()

        val annotationClass = when {
            meth.hasAnnotation<SlashCommand>() -> "slashCommand"
            meth.hasAnnotation<SlashSubCommand>() -> "slashSubCommand"
            else -> throw error("${meth.name} is not annotated with Slash(Sub)Command!")
        }

        /* find the slash context parameter. */
        val ctxParam = meth.valueParameters.firstOrNull {
            it.type.classifier?.equals(Context::class) == true
        }

        require(ctxParam != null) {
            "${meth.name} is missing the SlashContext parameter!"
        }

        /* find arguments */
        val parameters = meth.valueParameters.filterNot {
            it.type.classifier?.equals(Context::class) == true
        }

        val arguments = getArguments(parameters)
        return when (annotationClass) {
            "slashSubCommand" -> {
                val properties = meth.findAnnotation<SlashSubCommand>()!!
                SlashSubCommandFunction(name, meth, cog, arguments, ctxParam, declaringClass, properties)
            }

            "slashCommand" -> {
                val category = cog.name()
                    ?: cog::class.java.`package`.name.split('.')
                        .last()
                        .replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                val properties = meth.findAnnotation<SlashCommand>()!!
                SlashCommandFunction(name, meth, cog, arguments, ctxParam, category, properties, emptyList(), emptyMap())
            }

            else -> throw error("what")
        }
    }

    fun getArguments(parameters: List<KParameter>): List<CommandArgument> {
        val arguments = mutableListOf<CommandArgument>()
        for (p in parameters) {
            val name = p.findAnnotation<Name>()?.name
                ?: p.name
                ?: p.index.toString()

            val description = p.findAnnotation<Description>()?.value
                ?: "No description provided."

            val commandArgument = CommandArgument(
                name,
                p.type.jvmErasure.javaObjectType,
                null,
                p.isOptional,
                p.type.isMarkedNullable,
                false,
                CommandArgument.Option(description),
                p
            )

            arguments.add(commandArgument)
        }

        return arguments
    }
}
