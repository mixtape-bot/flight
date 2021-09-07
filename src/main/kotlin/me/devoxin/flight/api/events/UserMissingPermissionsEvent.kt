package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User

/**
 * Emitted when a user lacks [permissions] to execute [commandFunction]
 *
 * @param ctx
 *   The current command context.
 *
 * @param commandFunction
 *   The command
 *
 * @param permissions
 *   List of [Permission]s the user lacks.
 */
data class UserMissingPermissionsEvent(val ctx: MessageContext, val commandFunction: MessageCommandFunction, val permissions: List<Permission>) :
    Event {
    /**
     * The user that is lacking [permissions].
     */
    val user: User
        get() = ctx.author
}
