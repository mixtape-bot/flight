package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.Role
import java.util.*

object RoleParser : Parser<Role> {
    override suspend fun parse(ctx: Context, param: String): Optional<Role> {
        val snowflake = SnowflakeParser.parse(ctx, param)
        val role: Role? = if (snowflake.isPresent) {
            ctx.guild?.getRoleById(snowflake.get().resolved)
        } else {
            ctx.guild?.roleCache?.firstOrNull { it.name == param }
        }

        return Optional.ofNullable(role)
    }
}
