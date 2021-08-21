package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.VoiceChannel
import java.util.*

class VoiceChannelParser : Parser<VoiceChannel> {

    override suspend fun parse(ctx: Context, param: String): Optional<VoiceChannel> {
        val snowflake = SnowflakeParser.parse(ctx, param)

        /* test cache */
        var channel: VoiceChannel? = if (snowflake.isPresent) {
            ctx.guild?.getVoiceChannelById(snowflake.get().resolved)
        } else {
            ctx.guild?.voiceChannels?.firstOrNull { it.name == param }
        }

        return Optional.ofNullable(channel)
    }

}
