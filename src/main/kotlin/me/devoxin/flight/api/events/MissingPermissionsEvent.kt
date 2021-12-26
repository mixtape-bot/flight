package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
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
public data class MissingPermissionsEvent(val ctx: Context, val command: CommandFunction, val permissions: List<Permission>) : Event
