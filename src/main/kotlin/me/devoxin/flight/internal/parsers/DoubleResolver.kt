package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class DoubleResolver : Resolver<Double> {
    override val optionType: OptionType = OptionType.NUMBER

    override suspend fun getOptionChoice(name: String, value: Any): Command.Choice {
        require(value is Double) { "$value is not a double." }
        return Command.Choice(name, value.toDouble())
    }

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<Double> =
        Optional.of(option.asDouble)

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Double> {
        return Optional.ofNullable(param.toDoubleOrNull())
    }
}
