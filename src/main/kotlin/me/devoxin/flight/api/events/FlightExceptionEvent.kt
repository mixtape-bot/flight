package me.devoxin.flight.api.events

/**
 * Emitted when an internal error occurs within Flight.
 */
data class FlightExceptionEvent(val error: Throwable) : Event
