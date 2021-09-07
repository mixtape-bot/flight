package me.devoxin.flight.api.events

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.internal.entities.ICommand

/**
 * Emitted when the argument parser encounters an error.
 */
data class ParsingErrorEvent(
    val ctx: Context,
    val command: ICommand,
    val error: Throwable
) : Event
