package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.internal.entities.ICommand

/**
 * Emitted before a command is executed. Useful logging command usage etc.
 *
 * @param ctx
 *   The command context
 *
 * @param command
 *   The command being executed.
 */
data class CommandInvokedEvent(val ctx: Context, val command: ICommand) : Event
