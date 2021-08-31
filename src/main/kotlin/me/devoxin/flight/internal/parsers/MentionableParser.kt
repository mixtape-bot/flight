package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.types.Mentionable
import net.dv8tion.jda.api.entities.IMentionable
import java.util.*

class MentionableParser : Parser<Mentionable> {
    override suspend fun parse(ctx: Context, param: String): Optional<Mentionable> {
        val mentionable: IMentionable? = RoleParser.parse(ctx, param).orElse(null)
            ?: MemberParser.parse(ctx, param).orElse(null)
            ?: RoleParser.parse(ctx, param).orElse(null)

        return Optional.ofNullable(mentionable?.let { Mentionable(it) })
    }
}
