package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import me.devoxin.flight.internal.arguments.types.Mentionable
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class MentionableResolver : Resolver<Mentionable> {
    override val optionType: OptionType = OptionType.MENTIONABLE

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<Mentionable> =
        Optional.of(Mentionable(option.asMentionable))

    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<Mentionable> {
        val mentionable: IMentionable? = RoleResolver.parseContent(ctx, param).orElse(null)
            ?: MemberResolver.parseContent(ctx, param).orElse(null)
            ?: RoleResolver.parseContent(ctx, param).orElse(null)

        return Optional.ofNullable(mentionable?.let { Mentionable(it) })
    }
}
