package me.devoxin.flight.internal.arguments.resolvers.kotlin

import arrow.core.Option
import arrow.core.none
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.resolvers.Resolver
import me.devoxin.flight.internal.utils.some

public class BooleanResolver : Resolver<Boolean> {
    public companion object {
        public val trueExpr: List<String> = listOf("yes", "y", "true", "t", "1", "enable", "on")
        public val falseExpr: List<String> = listOf("no", "n", "false", "f", "0", "disable", "off")
    }

    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Boolean> =
        if (trueExpr.contains(content)) some(true) else if (falseExpr.contains(content)) some(false) else none()
}
