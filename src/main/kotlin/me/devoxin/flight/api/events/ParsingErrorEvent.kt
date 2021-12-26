package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context

/**
 * Emitted when the argument parser encounters an error.
 */
public data class ParsingErrorEvent(
    val ctx: Context,
    val command: CommandFunction,
    val error: Throwable
) : Event
