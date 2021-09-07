package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class FloatResolver : Resolver<Float> {
    override val optionType: OptionType = OptionType.NUMBER

    override suspend fun getOptionChoice(name: String, value: Any): Command.Choice {
        require (value is Float) { "$value is not a float." }
        return Command.Choice(name, value.toDouble())
    }

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<Float> =
        Optional.of(option.asDouble.toFloat())

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Float> {
        return Optional.ofNullable(param.toFloatOrNull())
    }
}
