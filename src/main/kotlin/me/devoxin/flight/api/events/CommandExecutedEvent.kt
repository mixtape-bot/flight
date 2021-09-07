package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.internal.entities.ICommand

/**
 * Emitted after a command has been executed, regardless of whether execution failed.
 *
 * @param ctx
 *   The command context
 *
 * @param command
 *   The command that was executed.
 *
 * @param failure
 *   Whether command execution failed. .
 */
data class CommandExecutedEvent(val ctx: Context, val command: ICommand, val failure: Boolean) : Event
