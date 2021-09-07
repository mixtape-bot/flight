package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext
import net.dv8tion.jda.api.Permission

/**
 * Emitted when we're lacking permissions to execute a command.
 *
 * @param ctx
 *   The current command context.
 *
 * @param commandFunction
 *   The command
 *
 * @param permissions
 *   List of [Permission]s we are lacking.
 */
data class MissingPermissionsEvent(val ctx: MessageContext, val commandFunction: MessageCommandFunction, val permissions: List<Permission>) : Event
