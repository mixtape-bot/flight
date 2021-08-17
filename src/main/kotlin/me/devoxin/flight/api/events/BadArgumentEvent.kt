package me.devoxin.flight.api.events

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.exceptions.BadArgument

/**
 * Emitted when an invalid argument is passed.
 */
class BadArgumentEvent(
    val ctx: Context,
    val command: CommandFunction,
    val error: BadArgument
) : Event
