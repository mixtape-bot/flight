package me.devoxin.flight.internal.arguments.resolvers.kotlin

import arrow.core.Option
import arrow.core.none
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.resolvers.Resolver
import me.devoxin.flight.internal.utils.some

public class StringResolver : Resolver<String> {
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<String> =
        if (content.isBlank()) none() else some(content)
}
