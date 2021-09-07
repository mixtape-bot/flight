package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.entities.DescriptionProvider
import me.devoxin.flight.api.entities.PrefixProvider
import me.devoxin.flight.api.events.Event
import me.devoxin.flight.api.ratelimit.RateLimitStrategy
import me.devoxin.flight.internal.entities.ICommand

data class FlightResources(
    val prefixes: PrefixProvider,
    val ratelimits: RateLimitStrategy,
    val ignoreBots: Boolean,
    val dispatcher: CoroutineDispatcher,
    val eventFlow: MutableSharedFlow<Event>,
    val doTyping: Boolean,
    val inhibitor: suspend (Context, ICommand) -> Boolean,
    val developers: MutableSet<Long>,
    val testGuilds: MutableSet<Long>,
    val descriptionProvider: DescriptionProvider
)
