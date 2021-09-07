package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

object RoleResolver : Resolver<Role> {
    override val optionType: OptionType = OptionType.ROLE

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<Role> =
        Optional.of(option.asRole)

    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<Role> {
        val snowflake = SnowflakeResolver.parseContent(ctx, param)
        val role: Role? = if (snowflake.isPresent) {
            ctx.guild?.getRoleById(snowflake.get().resolved)
        } else {
            ctx.guild?.roleCache?.firstOrNull { it.name == param }
        }

        return Optional.ofNullable(role)
    }
}
