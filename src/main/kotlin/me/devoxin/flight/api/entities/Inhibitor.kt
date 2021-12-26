package me.devoxin.flight.api.entities

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context

public fun interface Inhibitor {
    public suspend fun run(ctx: Context, command: CommandFunction): Boolean
}
