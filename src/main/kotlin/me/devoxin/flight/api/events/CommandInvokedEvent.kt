package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context

/**
 * Emitted before a command is executed. Useful logging command usage etc.
 *
 * @param ctx
 *   The command context
 *
 * @param command
 *   The command being executed.
 */
public data class CommandInvokedEvent(val ctx: Context, val command: CommandFunction) : Event
