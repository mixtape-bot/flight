package me.devoxin.flight.api

import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.RateLimit
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.entities.Executable
import java.util.HashMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

public class CommandFunction(
    name: String,
    public val category: String,
    public val properties: Command,
    public val rateLimit: RateLimit?,

    subCmds: List<SubCommandFunction>,
    // Executable properties
    method: KFunction<*>,
    cog: Cog,
    arguments: List<Argument>,
    contextParameter: KParameter
) : Executable(name, method, cog, arguments, contextParameter) {
    public val subcommands: HashMap<String, SubCommandFunction> = hashMapOf()

    private val triggers = listOf(name, *properties.aliases)
    private val triggerPattern = """(?i)^(${triggers.joinToString("|") { "\\Q$it\\E" }})($|\s.+$)""".toPattern()

    init {
        for (sc in subCmds) {
            val triggers = listOf(sc.name, *sc.properties.aliases)

            for (trigger in triggers) {
                if (subcommands.containsKey(trigger)) {
                    throw IllegalStateException("The sub-command trigger $trigger already exists!")
                }

                subcommands[trigger] = sc
            }
        }
    }

    public fun triggeredBy(content: String): Boolean {
        return triggerPattern.matcher(content).find()
    }

    public fun findTrigger(content: String): String? {
        val matcher = triggerPattern.matcher(content)
        return if (matcher.find()) matcher.group(1) else null
    }
}
