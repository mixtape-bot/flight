package me.devoxin.flight.internal.arguments.resolvers

import arrow.core.Option
import arrow.core.none
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Emoji
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.utils.some
import java.util.regex.Pattern

public class EmojiResolver : Resolver<Emoji> {
    public companion object {
        public val EMOJI_REGEX: Pattern = """<(a)?:([\w\d_]+):(\d{16,21})>""".toPattern()
    }

    // TODO: Support unicode emoji?
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Emoji> {
        val match = EMOJI_REGEX.matcher(content)
        if (match.find()) {
            val isAnimated = match.group(1) != null
            val name = match.group(2)
            val id = match.group(3).toLong()

            return some(Emoji(name, id, isAnimated))
        }

        return none()
    }
}
