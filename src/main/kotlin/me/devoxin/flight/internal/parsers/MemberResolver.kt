package me.devoxin.flight.internal.parsers

import kotlinx.coroutines.future.await
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

object MemberResolver : Resolver<Member> {
    override val optionType: OptionType = OptionType.USER

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<Member> =
        Optional.ofNullable(option.asMember)

    // TODO: Check ctx.message.mentionedMembers
    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<Member> {
        val snowflake = SnowflakeResolver.parseContent(ctx, param)

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
