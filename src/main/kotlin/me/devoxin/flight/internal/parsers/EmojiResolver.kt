package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.entities.Emoji
import java.util.*
import java.util.regex.Pattern

// TODO: maybe make this support slash commands?
class EmojiResolver : Resolver<Emoji> {
    // TODO: Support unicode emoji?
    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Emoji> {
        val match = EMOJI_REGEX.matcher(param)

        if (match.find()) {
            val isAnimated = match.group(1) != null
            val name = match.group(2)
            val id = match.group(3).toLong()

            return Optional.of(Emoji(name, id, isAnimated))
        }

        return Optional.empty()
    }

    companion object {
        val EMOJI_REGEX = Pattern.compile("<(a)?:(\\w+):(\\d{17,21})")!!
    }
}
