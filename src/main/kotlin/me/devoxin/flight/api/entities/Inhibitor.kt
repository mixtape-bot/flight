package me.devoxin.flight.api.entities

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.internal.entities.ICommand

typealias Inhibitor = suspend (Context, ICommand) -> Boolean
