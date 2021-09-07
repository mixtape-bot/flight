package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class LongResolver : Resolver<Long> {
    override val optionType: OptionType
        get() = OptionType.NUMBER

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<Long> =
        Optional.of(option.asLong)

    override suspend fun getOptionChoice(name: String, value: Any): Command.Choice {
        require (value is Long) { "$value is not a long." }
        return Command.Choice(name, value.toLong())
    }

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Long> {
        return Optional.ofNullable(param.toLongOrNull())
    }
}
