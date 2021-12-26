package me.devoxin.flight.internal.arguments.resolvers

import arrow.core.Option
import arrow.core.none
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Invite
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.utils.some
import java.util.regex.Pattern

public class InviteResolver : Resolver<Invite> {
    public companion object {
        public val INVITE_REGEX: Pattern = """discord(?:(?:app)?\.com/invite|\.gg)/([\w\d]{1,16})""".toPattern()
    }

    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Invite> {
        val match = INVITE_REGEX.matcher(content)
        if (match.find()) {
            val code = match.group(1)
            return some(Invite(ctx.jda, match.group(), code))
        }

        return none()
    }
}
