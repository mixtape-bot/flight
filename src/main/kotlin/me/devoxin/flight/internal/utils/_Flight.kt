package me.devoxin.flight.internal.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.devoxin.flight.api.Flight
import me.devoxin.flight.api.FlightBuilder
import me.devoxin.flight.api.events.Event
import org.slf4j.LoggerFactory
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
fun Flight(builder: FlightBuilder.() -> Unit): Flight {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return FlightBuilder().apply(builder).build()
}

@PublishedApi
internal val logger = LoggerFactory.getLogger("Flight.on")

/**
 * Convenience method that will invoke the [consumer] whenever [T] is emitted on [Flight.events]
 *
 * @param scope
 *   The scope to launch the consumer job in.
 *
 * @param consumer
 *   Event consumer for [T]
 *
 * @return A [Job] that can be used to cancel any further processing of [T]
 */
inline fun <reified T : Event> Flight.on(
    scope: CoroutineScope = this,
    crossinline consumer: suspend T.() -> Unit
): Job {
    return events.buffer(Channel.UNLIMITED).filterIsInstance<T>()
        .onEach { event ->
            launch {
                event
                    .runCatching { event.consumer() }
                    .onFailure { err -> logger.error("Error while handling event ${T::class.simpleName}", err) }
            }
        }
        .launchIn(scope)
}
