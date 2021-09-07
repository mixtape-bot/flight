package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext

/**
 * Emitted before a command is executed. Useful logging command usage etc.
 *
 * @param ctx
 *   The command context
 *
 * @param commandFunction
 *   The command being executed.
 */
data class CommandInvokedEvent(val ctx: MessageContext, val commandFunction: MessageCommandFunction) : Event
