package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.net.URL
import java.util.*

class UrlResolver : Resolver<URL> {
    override val optionType: OptionType = OptionType.STRING

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<URL> =
        parse(option.asString)

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<URL> =
        parse(param)

    private fun parse(param: String): Optional<URL> = try {
        Optional.of(URL(param))
    } catch (e: Throwable) {
        Optional.empty()
    }
}
