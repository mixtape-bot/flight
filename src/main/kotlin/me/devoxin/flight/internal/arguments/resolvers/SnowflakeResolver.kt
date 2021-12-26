package me.devoxin.flight.internal.arguments.resolvers

import arrow.core.Option
import arrow.core.none
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.arguments.types.Snowflake
import me.devoxin.flight.internal.utils.some
import java.util.regex.Pattern

public object SnowflakeResolver : Resolver<Snowflake> {
    private val snowflakeMatch = Pattern.compile("""^(?:<(?:@!?|@&|#!?|a?:.+:)(?<sid>\d{16,21})>|(?<id>\d{16,21}))$""")

    override suspend fun resolve(ctx: Context, content: String, argument: Argument): Option<Snowflake> {
        val match = snowflakeMatch.matcher(content)
        if (match.matches()) {
            val id = match.group("sid")
                ?: match.group("id")

            return some(Snowflake(value = id.toLong()))
        }

        return none()
    }
}
