package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class VoiceChannelResolver : Resolver<VoiceChannel> {
    override val optionType: OptionType = OptionType.CHANNEL

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<VoiceChannel> =
        Optional.ofNullable(option.asGuildChannel as? VoiceChannel)

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<VoiceChannel> {
        val snowflake = SnowflakeResolver.resolve(ctx, param)

        /* test cache */
        var channel: VoiceChannel? = if (snowflake.isPresent) {
            ctx.guild?.getVoiceChannelById(snowflake.get().resolved)
        } else {
            ctx.guild?.voiceChannels?.firstOrNull { it.name == param }
        }

        return Optional.ofNullable(channel)
    }

}
