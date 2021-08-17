package me.devoxin.flight.api.annotations

import me.devoxin.flight.api.ratelimit.RateLimitType
import java.util.concurrent.TimeUnit

/**
 * Sets a rate-limit on the command.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RateLimit(
    /** How long the rate-limit lasts. */
    val duration: Long,
    /** The time unit of the duration. */
    val durationUnit: TimeUnit = TimeUnit.MILLISECONDS,
    /** The bucket this rate-limit applies to. */
    val type: RateLimitType
)
