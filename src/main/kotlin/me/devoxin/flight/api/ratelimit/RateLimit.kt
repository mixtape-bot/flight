package me.devoxin.flight.api.ratelimit

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.RateLimit

data class RateLimit(val name: String, val expiresAt: Long) {
    companion object {
        fun getEntityId(options: RateLimit, ctx: Context): Long? = when (options.type) {
            RateLimitType.USER -> ctx.author.idLong
            RateLimitType.GUILD -> ctx.guild?.idLong
            RateLimitType.CHANNEL -> ctx.messageChannel.idLong
            RateLimitType.GLOBAL -> -1
        }
    }
}
