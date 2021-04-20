package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.BucketType

/**
 * Emitted when a command is invoked while on cool-down
 *
 * @param ctx
 *   The current command context.
 *
 * @param command
 *   The command that is on cool-down
 *
 * @param remaining
 *   The remaining time of the cool-down, in milliseconds
 *
 * @param entity
 *   ID of the entity that is being rate-limited, or -1 if global rate-limited.
 */
class CommandRateLimitedEvent(val ctx: Context, val command: CommandFunction, val remaining: Long, val entity: Long) :
  Event {
  val type: BucketType
    get() = command.cooldown!!.bucket
}
