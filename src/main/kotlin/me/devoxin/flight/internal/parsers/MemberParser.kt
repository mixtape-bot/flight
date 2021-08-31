package me.devoxin.flight.internal.parsers

import kotlinx.coroutines.future.await
import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.Member
import java.util.*

object MemberParser : Parser<Member> {
    // TODO: Check ctx.message.mentionedMembers
    override suspend fun parse(ctx: Context, param: String): Optional<Member> {
        val snowflake = SnowflakeParser.parse(ctx, param)

        /* firstly, test the cache. */
        var member: Member? = if (snowflake.isPresent) {
            ctx.guild?.getMemberById(snowflake.get().resolved)
        } else {
            if (param.length > 5 && param[param.length - 5].toString() == "#") {
                val tag = param.split("#")
                ctx.guild?.memberCache?.find { it.user.name == tag[0] && it.user.discriminator == tag[1] }
            } else {
                ctx.guild?.getMembersByName(param, false)?.firstOrNull()
            }
        }

        /* finally, try to retrieve the voice channel through rest. */
        if (member == null && snowflake.isPresent) {
            snowflake.get().resolved.runCatching {
                member = ctx.guild?.retrieveMemberById(this)
                    ?.submit()
                    ?.await()
            }
        }

        return Optional.ofNullable(member)
    }
}
