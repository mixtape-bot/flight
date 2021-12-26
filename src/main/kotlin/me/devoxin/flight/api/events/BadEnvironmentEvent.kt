package me.devoxin.flight.api.events

import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.entities.Executable

/**
 * Emitted when an [Executable] is executed in a bad environment.
 *
 * @param ctx
 *   The [Context] instance.
 *
 * @param command
 *   The [Executable] that was executed.
 *
 * @param reason
 *   The reason it was a bad environment.
 */
public data class BadEnvironmentEvent(
    val ctx: Context,
    val command: Executable,
    val reason: Reason
) : Event {
    public enum class Reason {
        NonNSFW,
        NonGuild,
        NonDeveloper,
    }
}
