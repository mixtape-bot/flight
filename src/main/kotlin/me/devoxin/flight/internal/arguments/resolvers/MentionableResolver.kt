package me.devoxin.flight.internal.arguments.resolvers

import arrow.core.Option
import arrow.core.orElse
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.types.Mentionable
import me.devoxin.flight.internal.arguments.resolvers.jda.MemberResolver
import me.devoxin.flight.internal.arguments.resolvers.jda.RoleResolver

public class MentionableResolver : Resolver<Mentionable> {
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Mentionable> {
        return RoleResolver.resolve(ctx, content, argument)
            .orElse { MemberResolver.resolve(ctx, content, argument) }
            .map { Mentionable(it) }
    }
}
