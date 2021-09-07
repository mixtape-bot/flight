package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.internal.entities.ICommand
import net.dv8tion.jda.api.Permission

/**
 * Emitted when we're lacking permissions to execute a command.
 *
 * @param ctx
 *   The current command context.
 *
 * @param command
 *   The command
 *
 * @param permissions
 *   List of [Permission]s we are lacking.
 */
data class MissingPermissionsEvent(val ctx: Context, val command: ICommand, val permissions: List<Permission>) : Event
