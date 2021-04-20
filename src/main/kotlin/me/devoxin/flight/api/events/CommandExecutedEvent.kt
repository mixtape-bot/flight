package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context

/**
 * Emitted after a command has been executed, regardless of whether execution failed.
 *
 * @param ctx
 *   The command context
 *
 * @param command
 *   The command that was executed.
 *
 * @param failed
 *   Whether execution had failed.
 */
class CommandExecutedEvent(val ctx: Context, val command: CommandFunction, val failed: Boolean) : Event
