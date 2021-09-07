package me.devoxin.flight.api.ratelimit

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.annotations.RateLimit

data class RateLimit(val name: String, val expiresAt: Long) {
    companion object {
        fun getEntityId(options: RateLimit, ctx: MessageContext): Long? = when (options.type) {
            RateLimitType.USER -> ctx.author.idLong
            RateLimitType.GUILD -> ctx.guild?.idLong
            RateLimitType.CHANNEL -> ctx.channel.idLong
            RateLimitType.GLOBAL -> -1
        }
    }
}
