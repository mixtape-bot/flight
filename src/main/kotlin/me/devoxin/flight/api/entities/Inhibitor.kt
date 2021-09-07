package me.devoxin.flight.api.entities

import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext

typealias Inhibitor = suspend (MessageContext, MessageCommandFunction) -> Boolean
