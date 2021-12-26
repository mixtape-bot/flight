package me.devoxin.flight.api.ratelimit

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

public class DefaultRatelimitManager : RatelimitManager {
    private val buckets = ConcurrentHashMap<RatelimitType, Bucket>()

    override fun isRatelimited(id: Long, type: RatelimitType, commandName: String): Boolean {
        return buckets[type]?.isRateLimited(id, commandName) ?: false
    }

    override fun getExpirationDate(id: Long, type: RatelimitType, commandName: String): Long {
        return buckets[type]?.getExpirationDate(id, commandName) ?: 0
    }

    override fun putRatelimit(id: Long, type: RatelimitType, duration: Long, commandName: String) {
        buckets.computeIfAbsent(type) { Bucket() }.putRatelimit(id, duration, commandName)
    }

    private class Bucket {
        private val sweeperThread = Executors.newSingleThreadScheduledExecutor()
        private val rateLimits = ConcurrentHashMap<Long, MutableSet<Ratelimit>>() // EntityID => [Commands...]

        fun isRateLimited(id: Long, commandName: String): Boolean {
            return getExpirationDate(id, commandName) > 0
        }

        fun getExpirationDate(id: Long, commandName: String): Long {
            val rateLimit = rateLimits[id]?.firstOrNull { it.name == commandName }
                ?: return 0

            return min(rateLimit.expiresAt - System.currentTimeMillis(), 0)
        }

        fun putRatelimit(id: Long, time: Long, commandName: String) {
            val cds = rateLimits.computeIfAbsent(id) { mutableSetOf() }
            val rateLimit = Ratelimit(commandName, System.currentTimeMillis() + time)
            cds.add(rateLimit)

            sweeperThread.schedule({ cds.remove(rateLimit) }, time, TimeUnit.MILLISECONDS)
        }
    }

}
