package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.entities.PrefixProvider
import me.devoxin.flight.api.events.Event
import me.devoxin.flight.api.ratelimit.RateLimitStrategy

data class FlightResources(
    val prefixes: PrefixProvider,
    val ratelimits: RateLimitStrategy,
    val ignoreBots: Boolean,
    val dispatcher: CoroutineDispatcher,
    val eventFlow: MutableSharedFlow<Event>,
    val doTyping: Boolean,
    val inhibitor: suspend (MessageContext, MessageCommandFunction) -> Boolean,
    val developers: MutableSet<Long>,
    val testGuilds: MutableSet<Long>
)
