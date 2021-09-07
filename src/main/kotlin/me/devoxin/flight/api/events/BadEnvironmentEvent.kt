package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.internal.entities.ICommand

/**
 * Emitted when an [ICommand] is executed in a bad environment.
 *
 * @param ctx
 *   The [Context] instance.
 *
 * @param command
 *   The [ICommand] that was executed.
 *
 * @param reason
 *   The reason it was a bad environment.
 */
data class BadEnvironmentEvent(
    val ctx: Context,
    val command: ICommand,
    val reason: Reason
) : Event {
    enum class Reason {
        NonNSFW,
        NonGuild,
        NonDeveloper,
    }
}
