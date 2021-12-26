package me.devoxin.flight.internal.arguments.resolvers.jda

import arrow.core.Option
import arrow.core.orElse
import arrow.core.toOption
import kotlinx.coroutines.future.await
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.resolvers.Resolver
import me.devoxin.flight.internal.arguments.resolvers.SnowflakeResolver
import me.devoxin.flight.internal.utils.unwrap
import net.dv8tion.jda.api.entities.User

public class UserResolver : Resolver<User> {
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<User> {
        val snowflake = SnowflakeResolver.resolve(ctx, content, argument)

        /* firstly, test the cache. */
        var user = if (snowflake.isDefined()) {
            val id = snowflake.unwrap().value;
            ctx.message.mentionedUsers.find { it.idLong == id }.toOption()
                .orElse { ctx.jda.getUserById(id).toOption() }
                .orNull()
        } else {
            if (content.length > 5 && content[content.length - 5].toString() == "#") {
                val tag = content.split("#")
                ctx.jda.userCache.find { it.name == tag[0] && it.discriminator == tag[1] }
            } else {
                ctx.jda
                    .getUsersByName(content, false)
                    .firstOrNull()
            }
        }

        /* finally, see if we can retrieve by rest. */
        if (user == null && snowflake.isDefined()) {
            user = snowflake.unwrap()
                .runCatching { ctx.jda.retrieveUserById(value).submit().await() }
                .getOrNull()
        }

        return user.toOption()
    }

}
