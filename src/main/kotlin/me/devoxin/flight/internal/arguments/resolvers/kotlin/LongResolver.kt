package me.devoxin.flight.internal.arguments.resolvers.kotlin

import arrow.core.Option
import arrow.core.toOption
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.resolvers.Resolver

public class LongResolver : Resolver<Long> {
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Long> =
        content.toLongOrNull().toOption()
}
