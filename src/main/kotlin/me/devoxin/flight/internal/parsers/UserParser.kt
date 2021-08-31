package me.devoxin.flight.internal.parsers

import kotlinx.coroutines.future.await
import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.User
import java.util.*

object UserParser : Parser<User> {
    // TODO: Check ctx.message.mentionedUsers
    override suspend fun parse(ctx: Context, param: String): Optional<User> {
        val snowflake = SnowflakeParser.parse(ctx, param)

        /* firstly, test the cache. */
        var user = if (snowflake.isPresent) {
            ctx.jda.getUserById(snowflake.get().resolved)
        } else {
            if (param.length > 5 && param[param.length - 5].toString() == "#") {
                val tag = param.split("#")
                ctx.jda.userCache.find { it.name == tag[0] && it.discriminator == tag[1] }
            } else {
                ctx.jda.userCache.find { it.name == param }
            }
        }

        /* finally, see if we can retrieve by rest. */
        if (user == null && snowflake.isPresent) {
            snowflake.get().resolved.runCatching {
                user = ctx.jda.retrieveUserById(this)
                    .submit()
                    .await()
            }
        }

        return Optional.ofNullable(user)
    }

}
