package me.devoxin.flight.api

import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.annotations.GreedyType
import me.devoxin.flight.api.annotations.RateLimit
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.entities.Executable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class CommandFunction(
    name: String,
    val category: String,
    val properties: Command,
    val rateLimit: RateLimit?,

    subCmds: List<SubCommandFunction>,
    // Executable properties
    method: KFunction<*>,
    cog: Cog,
    arguments: List<Argument>,
    contextParameter: KParameter
) : Executable(name, method, cog, arguments, contextParameter) {

    val subcommands = hashMapOf<String, SubCommandFunction>()

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

}
