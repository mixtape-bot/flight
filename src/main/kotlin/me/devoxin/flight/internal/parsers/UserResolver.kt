package me.devoxin.flight.internal.parsers

import kotlinx.coroutines.future.await
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

object UserResolver : Resolver<User> {
    override val optionType: OptionType = OptionType.USER

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<User> =
        Optional.of(option.asUser)

    // TODO: Check ctx.message.mentionedUsers
    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<User> {
        val snowflake = SnowflakeResolver.parseContent(ctx, param)

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
