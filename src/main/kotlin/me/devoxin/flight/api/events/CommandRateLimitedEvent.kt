package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.ratelimit.RateLimitType

/**
 * Emitted when a command is invoked while on rate-limit
 *
 * @param ctx
 *   The current command context.
 *
 * @param command
 *   The command that is on rate-limit
 *
 * @param remaining
 *   The remaining time of the rate-limit, in milliseconds
 *
 * @param entity
 *   ID of the entity that is being rate-limited, or -1 if global rate-limited.
 */
data class CommandRateLimitedEvent(val ctx: Context, val command: CommandFunction, val remaining: Long, val entity: Long) :
    Event {
    val type: RateLimitType
        get() = command.rateLimit!!.type
}
