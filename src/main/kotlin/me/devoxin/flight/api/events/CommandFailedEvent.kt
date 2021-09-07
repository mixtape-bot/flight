package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.internal.entities.ICommand

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
data class CommandFailedEvent(val ctx: Context, val command: ICommand, val error: Throwable) : Event
