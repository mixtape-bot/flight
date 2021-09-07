package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.internal.entities.ICommand

/**
 * Emitted when an invalid argument is passed.
 */
data class BadArgumentEvent(
    val ctx: Context,
    val command: ICommand,
    val error: BadArgument
) : Event
