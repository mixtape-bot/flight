package me.devoxin.flight.internal.arguments.resolvers

import arrow.core.Option
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument

public fun interface Resolver<T> {
    /**
     * Resolves the provided content into [T]
     *
     * @param ctx      The local context.
     * @param content  The content to resolve.
     * @param argument Argument this is for
     */
    public suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<T>

    /**
     *
     */
    public suspend fun resolveCatching(ctx: Context, content: String, argument: Argument): Result<Option<T>> =
        runCatching { resolve(ctx, content, argument) }
}
