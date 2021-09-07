package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class IntResolver : Resolver<Int> {
    override val optionType: OptionType = OptionType.INTEGER

    override suspend fun getOptionChoice(name: String, value: Any): Command.Choice {
        require(value is Int) { "$value is not an integer" }
        return Command.Choice(name, value.toLong())
    }

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<Int> =
        Optional.of(option.asLong.toInt())

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Int> {
        return Optional.ofNullable(param.toIntOrNull())
    }
}
