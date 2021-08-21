package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

class TextChannelParser : Parser<TextChannel> {
    override suspend fun parse(ctx: Context, param: String): Optional<TextChannel> {
        val snowflake = SnowflakeParser.parse(ctx, param)
        val channel: TextChannel? = if (snowflake.isPresent) {
            ctx.guild?.getTextChannelById(snowflake.get().resolved)
        } else {
            ctx.guild?.textChannels?.firstOrNull { it.name == param }
        }

        return Optional.ofNullable(channel)
    }
}
