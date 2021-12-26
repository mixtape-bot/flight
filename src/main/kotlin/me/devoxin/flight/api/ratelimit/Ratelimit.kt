package me.devoxin.flight.api.ratelimit

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.RateLimit

public data class Ratelimit(val name: String, val expiresAt: Long) {
    public companion object {
        public fun getEntityId(options: RateLimit, ctx: Context): Long? = when (options.type) {
            RatelimitType.USER -> ctx.author.idLong
            RatelimitType.GUILD -> ctx.guild?.idLong
            RatelimitType.CHANNEL -> ctx.channel.idLong
            RatelimitType.GLOBAL -> -1
        }
    }
}
