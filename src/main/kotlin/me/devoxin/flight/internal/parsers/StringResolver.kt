package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class StringResolver : Resolver<String> {
    override val optionType: OptionType = OptionType.STRING

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<String> =
        Optional.of(option.asString)

    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<String> {
        if (param.isEmpty() || param.isBlank()) {
            return Optional.empty()
        }

        return Optional.of(param)
    }
}
