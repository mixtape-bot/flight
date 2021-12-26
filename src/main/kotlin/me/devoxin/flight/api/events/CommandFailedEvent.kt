package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context

/**
 * Emitted when a command encountered an error during execution.
 *
 * @param ctx
 *   The command context
 *
 * @param command
 *   The command that had failed to execute
 *
 * @param error
 *   The error encountered
 */
public data class CommandFailedEvent(val ctx: Context, val command: CommandFunction, val error: Throwable) : Event
