package me.devoxin.flight.api.command.message

import me.devoxin.flight.api.command.message.annotations.MessageCommand
import me.devoxin.flight.api.annotations.RateLimit
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import me.devoxin.flight.internal.entities.ICommand
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

data class MessageCommandFunction(
    override val name: String,
    override val method: KFunction<*>,
    override val cog: Cog,
    override val arguments: List<CommandArgument>,
    override val contextParameter: KParameter,
    override val category: String,
    val properties: MessageCommand,
    val rateLimit: RateLimit?,
    val subCommands: List<MessageSubCommandFunction>
) : ICommand.Message, ICommand.Categorized {
    val subcommands = hashMapOf<String, MessageSubCommandFunction>()

    init {
        for (sc in subCommands) {
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
