package me.devoxin.flight.api

import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.entities.Executable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

public class SubCommandFunction(
    name: String,
    public val properties: SubCommand,
    // Executable properties
    method: KFunction<*>,
    cog: Cog,
    arguments: List<Argument>,
    contextParameter: KParameter
) : Executable(name, method, cog, arguments, contextParameter) {
    private val triggers = listOf(name, *properties.aliases)
    private val triggerPattern = """(?i)^(${triggers.joinToString("|") { "\\Q$it\\E" }})($|\s.+$)""".toPattern()

    public fun triggeredBy(content: String): Boolean {
        return triggerPattern.matcher(content).find()
    }

    public fun findTrigger(content: String): String? {
        val matcher = triggerPattern.matcher(content)
        return if (matcher.find()) matcher.group(1) else null
    }
}
