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
import net.dv8tion.jda.api.entities.Member

public open class MemberResolver : Resolver<Member> {
    public companion object : MemberResolver()

    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Member> {
        val snowflake = SnowflakeResolver.resolve(ctx, content, argument)

        /* firstly, test the cache. */
        var member: Member? = if (snowflake.isDefined()) {
            snowflake
                .mapNotNull { s -> ctx.message.mentionedMembers.find { it.idLong == s.value } }
                .orElse { ctx.guild?.getMemberById(snowflake.unwrap().value).toOption() }
                .unwrap()
        } else {
            if (content.length > 5 && content[content.length - 5].toString() == "#") {
                val tag = content.split("#")
                ctx.guild?.memberCache?.find { it.user.name == tag[0] && it.user.discriminator == tag[1] }
            } else {
                ctx.guild?.getMembersByName(content, false)?.firstOrNull()
            }
        }

        /* finally, try to retrieve the voice channel through rest. */
        if (member == null && snowflake.isDefined()) {
            member = snowflake.unwrap()
                .runCatching { ctx.guild?.retrieveMemberById(value)?.submit()?.await() }
                .getOrNull()
        }

        return member.toOption()
    }
}
