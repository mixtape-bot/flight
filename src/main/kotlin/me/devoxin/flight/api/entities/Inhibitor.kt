package me.devoxin.flight.api.entities

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context

typealias Inhibitor = suspend (Context, CommandFunction) -> Boolean