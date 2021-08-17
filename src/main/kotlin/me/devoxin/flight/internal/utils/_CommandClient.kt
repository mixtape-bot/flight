package me.devoxin.flight.internal.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.devoxin.flight.api.CommandClient
import me.devoxin.flight.api.CommandClientBuilder
import me.devoxin.flight.api.events.Event
import org.slf4j.LoggerFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates a new [CommandClient] instance using the provided [builder]
 *
 * @param builder
 *   Used to create a new [CommandClient] instance.
 */
@OptIn(ExperimentalContracts::class)
fun CommandClient(builder: CommandClientBuilder.() -> Unit): CommandClient {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return CommandClientBuilder().apply(builder).build()
}

@PublishedApi
internal val log = LoggerFactory.getLogger("CommandClient.on")

/**
 * Convenience method that will invoke the [consumer] whenever [T] is emitted on [CommandClient.events]
 *
 * @param scope
 *   The scope to launch the consumer job in.
 *
 * @param consumer
 *   Event consumer for [T]
 *
 * @return A [Job] that can be used to cancel any further processing of [T]
 */
inline fun <reified T : Event> CommandClient.on(
    scope: CoroutineScope = this,
    crossinline consumer: suspend T.() -> Unit
): Job {
    return events.buffer(Channel.UNLIMITED).filterIsInstance<T>()
        .onEach { event ->
            launch {
                event
                    .runCatching { event.consumer() }
                    .onFailure { err -> log.error("Error while handling event ${T::class.simpleName}", err) }
            }
        }
        .launchIn(scope)
}
