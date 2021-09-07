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

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<Mentionable> =
        Optional.of(Mentionable(option.asMentionable))

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Mentionable> {
        val mentionable: IMentionable? = RoleResolver.resolve(ctx, param).orElse(null)
            ?: MemberResolver.resolve(ctx, param).orElse(null)
            ?: RoleResolver.resolve(ctx, param).orElse(null)

        return Optional.ofNullable(mentionable?.let { Mentionable(it) })
    }
}
