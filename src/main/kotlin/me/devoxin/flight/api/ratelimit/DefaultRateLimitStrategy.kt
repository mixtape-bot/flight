package me.devoxin.flight.api.ratelimit

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class DefaultRateLimitStrategy : RateLimitStrategy {
    private val buckets = ConcurrentHashMap<RateLimitType, Bucket>()

    override fun isRateLimited(id: Long, type: RateLimitType, commandName: String): Boolean {
        return buckets[type]?.isRateLimited(id, commandName) ?: false
    }

    override fun getExpirationDate(id: Long, type: RateLimitType, commandName: String): Long {
        return buckets[type]?.getExpirationDate(id, commandName) ?: 0
    }

    override fun putRateLimit(id: Long, type: RateLimitType, duration: Long, commandName: String) {
        buckets.computeIfAbsent(type) { Bucket() }.putRateLimit(id, duration, commandName)
    }

    class Bucket {
        private val sweeperThread = Executors.newSingleThreadScheduledExecutor()
        private val rateLimits = ConcurrentHashMap<Long, MutableSet<RateLimit>>() // EntityID => [Commands...]

        fun isRateLimited(id: Long, commandName: String): Boolean {
            return getExpirationDate(id, commandName) > 0
        }

        fun getExpirationDate(id: Long, commandName: String): Long {
            val rateLimit = rateLimits[id]?.firstOrNull { it.name == commandName }
                ?: return 0
            return abs(rateLimit.expiresAt - System.currentTimeMillis())
        }

        fun putRateLimit(id: Long, time: Long, commandName: String) {
            val cds = rateLimits.computeIfAbsent(id) { mutableSetOf() }
            val rateLimit = RateLimit(commandName, System.currentTimeMillis() + time)
            cds.add(rateLimit)

            sweeperThread.schedule({ cds.remove(rateLimit) }, time, TimeUnit.MILLISECONDS)
        }
    }

}
