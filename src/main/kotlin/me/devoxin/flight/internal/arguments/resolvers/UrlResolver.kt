package me.devoxin.flight.internal.arguments.resolvers

import arrow.core.Option
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.utils.toOption
import java.net.URL

public class UrlResolver : Resolver<URL> {
    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<URL> {
        return content
            .runCatching { URL(content) }
            .toOption()
    }
}
