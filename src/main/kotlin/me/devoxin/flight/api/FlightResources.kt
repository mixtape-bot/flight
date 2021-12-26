package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import me.devoxin.flight.api.entities.Inhibitor
import me.devoxin.flight.api.entities.PrefixProvider
import me.devoxin.flight.api.events.Event
import me.devoxin.flight.api.ratelimit.RatelimitManager

public data class FlightResources(
    val prefixProvider: PrefixProvider,
    val ratelimitManager: RatelimitManager,
    val ignoreBots: Boolean,
    val dispatcher: CoroutineDispatcher,
    val eventFlow: MutableSharedFlow<Event>,
    val doTyping: Boolean,
    val inhibitor: Inhibitor,
    val developers: MutableSet<Long>
)
