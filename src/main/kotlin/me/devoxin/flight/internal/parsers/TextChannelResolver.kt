package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class TextChannelResolver : Resolver<TextChannel> {
    override val optionType: OptionType = OptionType.CHANNEL

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<TextChannel> =
        Optional.ofNullable(option.asGuildChannel as? TextChannel)

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<TextChannel> {
        val snowflake = SnowflakeResolver.resolve(ctx, param)
        val channel: TextChannel? = if (snowflake.isPresent) {
            ctx.guild?.getTextChannelById(snowflake.get().resolved)
        } else {
            ctx.guild?.textChannels?.firstOrNull { it.name == param }
        }

        return Optional.ofNullable(channel)
    }
}
