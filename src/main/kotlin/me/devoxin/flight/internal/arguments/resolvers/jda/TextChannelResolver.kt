package me.devoxin.flight.internal.arguments.resolvers.jda

import arrow.core.Option
import arrow.core.orElse
import arrow.core.toOption
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.resolvers.Resolver
import me.devoxin.flight.internal.arguments.resolvers.SnowflakeResolver
import me.devoxin.flight.internal.utils.unwrap
import net.dv8tion.jda.api.entities.TextChannel

public class TextChannelResolver : Resolver<TextChannel> {
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<TextChannel> {
        val snowflake = SnowflakeResolver.resolve(ctx, content, argument)

        return if (snowflake.isDefined()) {
            snowflake
                .mapNotNull { s -> ctx.message.mentionedTextChannels.find { it.idLong == s.value } }
                .orElse { ctx.guild?.getTextChannelById(snowflake.unwrap().value).toOption() }
        } else {
            ctx.guild
                ?.getTextChannelsByName(content, true)
                ?.firstOrNull()
                .toOption()
        }
    }
}
