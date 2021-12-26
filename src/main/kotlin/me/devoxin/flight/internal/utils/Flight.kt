package me.devoxin.flight.internal.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.devoxin.flight.api.Flight
import me.devoxin.flight.api.FlightBuilder
import me.devoxin.flight.api.events.Event
import mu.KLogger
import mu.KotlinLogging
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates a new [Flight] instance using the provided [builder]
 *
 * @param builder
 *   Used to create a new [Flight] instance.
 */
@OptIn(ExperimentalContracts::class)
public inline fun Flight(builder: FlightBuilder.() -> Unit = {}): Flight {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return FlightBuilder()
        .apply(builder)
        .build()
}

@PublishedApi
internal val flightOnLogger: KLogger = KotlinLogging.logger("Flight.on")

/**
 * Convenience method that will invoke the [block] whenever [T] is emitted on [Flight.events]
 *
 * @param scope
 *   The scope to launch the consumer job in.
 *
 * @param block
 *   Event consumer for [T]
 *
 * @return A [Job] that can be used to cancel any further processing of [T]
 */
public inline fun <reified T : Event> Flight.on(
    scope: CoroutineScope = this,
    noinline block: suspend T.() -> Unit
): Job {
    return events.buffer(Channel.UNLIMITED).filterIsInstance<T>()
        .onEach { event ->
            event
                .runCatching { block() }
                .onFailure { err -> flightOnLogger.error(err) { "Error while handling event ${T::class.simpleName}" } }
        }
        .launchIn(scope)
}
