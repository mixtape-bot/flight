package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class FloatResolver : Resolver<Float> {
    override val optionType: OptionType = OptionType.NUMBER

    override suspend fun getChoiceValue(name: String, value: Any): Command.Choice {
        require (value is Float) { "$value is not a float." }
        return Command.Choice(name, value.toDouble())
    }

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<Float> =
        Optional.of(option.asDouble.toFloat())

    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<Float> {
        return Optional.ofNullable(param.toFloatOrNull())
    }
}
