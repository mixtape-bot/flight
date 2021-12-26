package me.devoxin.flight.internal.arguments.resolvers.jda

import arrow.core.Option
import arrow.core.orElse
import arrow.core.toOption
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.resolvers.Resolver
import me.devoxin.flight.internal.arguments.resolvers.SnowflakeResolver
import net.dv8tion.jda.api.entities.Role

public open class RoleResolver : Resolver<Role> {
    public companion object : RoleResolver()

    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Role> {
        return SnowflakeResolver.resolve(ctx, content, argument)
            .mapNotNull { ctx.guild?.getRoleById(it.value) }
            .orElse { ctx.guild?.roleCache?.firstOrNull { it.name == content }.toOption() }
    }
}
