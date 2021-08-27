package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import me.devoxin.flight.api.entities.*
import me.devoxin.flight.api.entities.Emoji
import me.devoxin.flight.api.entities.Invite
import me.devoxin.flight.api.events.Event
import me.devoxin.flight.api.ratelimit.RateLimitStrategy
import me.devoxin.flight.api.ratelimit.DefaultRateLimitStrategy
import me.devoxin.flight.internal.arguments.ArgParser
import me.devoxin.flight.internal.arguments.types.Snowflake
import me.devoxin.flight.internal.parsers.*
import net.dv8tion.jda.api.entities.*
import java.net.URL
import java.util.concurrent.Executors

class FlightBuilder {
    /**
     * Strings that messages must start with to trigger the bot.
     */
    var prefixes: List<String> = emptyList()

    /**
     * Whether the bot will allow mentions to be used as a prefix.
     */
    var allowMentionPrefix: Boolean = true

    /**
     * Whether bots and webhooks should be ignored. The recommended option is true to prevent feedback loops.
     */
    var ignoreBots: Boolean = true

    /**
     * The provider used for obtaining prefixes
     */
    var prefixProvider: PrefixProvider? = null

    /**
     * The provider used for rate-limits.
     */
    var ratelimitProvider: RateLimitStrategy? = null

    /**
     * The coroutine dispatcher used for executing commands.
     */
    var dispatcher: CoroutineDispatcher? = null

    /**
     * The number of threads used for the default [dispatcher]
     */
    var commandThreads: Int = Runtime.getRuntime().availableProcessors() * 2

    /**
     * Determines whether a command should be executed or not.
     */
    var inhibitor: Inhibitor = { _, _ -> true }

    /**
     * Used to emit several [Event]s
     */
    var eventFlow: MutableSharedFlow<Event>? = null

    /**
     * Whether to send typing events during command execution.
     */
    var doTyping: Boolean = false

    /**
     * A list of IDs as the developers. Any users with the given IDs
     * are then able to use commands marked with `developerOnly`.
     */
    val developers: MutableSet<Long> = mutableSetOf()

    /**
     * Registers an argument parser to the given class.
     *
     * @return The builder instance. Useful for chaining.
     */
    fun parser(klass: Class<*>, parser: Parser<*>): FlightBuilder {
        // This is kinda unsafe. Would use T, but nullable/boxed types revert
        // to their java.lang counterparts. E.g. Int? becomes java.lang.Integer,
        // but Int remains kotlin.Int.
        // See https://youtrack.jetbrains.com/issue/KT-35423

        ArgParser.parsers[klass] = parser
        return this
    }

    inline fun <reified T> parser(parser: Parser<T>) = parser(T::class.java, parser)

    /**
     * Registers all default argument parsers.
     *
     * @return The builder instance. Useful for chaining.
     */
    fun registerDefaultParsers(): FlightBuilder {
        /* Kotlin types and primitives */
        val booleanParser = BooleanParser()
        ArgParser.parsers[Boolean::class.java] = booleanParser
        ArgParser.parsers[java.lang.Boolean::class.java] = booleanParser

        val doubleParser = DoubleParser()
        ArgParser.parsers[Double::class.java] = doubleParser
        ArgParser.parsers[java.lang.Double::class.java] = doubleParser

        val floatParser = FloatParser()
        ArgParser.parsers[Float::class.java] = floatParser
        ArgParser.parsers[java.lang.Float::class.java] = floatParser

        val intParser = IntParser()
        ArgParser.parsers[Int::class.java] = intParser
        ArgParser.parsers[java.lang.Integer::class.java] = intParser

        val longParser = LongParser()
        ArgParser.parsers[Long::class.java] = longParser
        ArgParser.parsers[java.lang.Long::class.java] = longParser

        /* JDA entities */
        val inviteParser = InviteParser()
        ArgParser.parsers[Invite::class.java] = inviteParser
        ArgParser.parsers[net.dv8tion.jda.api.entities.Invite::class.java] = inviteParser

        ArgParser.parsers[Member::class.java] = MemberParser()
        ArgParser.parsers[Role::class.java] = RoleParser()
        ArgParser.parsers[TextChannel::class.java] = TextChannelParser()
        ArgParser.parsers[User::class.java] = UserParser()
        ArgParser.parsers[VoiceChannel::class.java] = VoiceChannelParser()

        /* Custom entities */
        ArgParser.parsers[Emoji::class.java] = EmojiParser()
        ArgParser.parsers[String::class.java] = StringParser()
        ArgParser.parsers[Snowflake::class.java] = SnowflakeParser

        /* java tings */
        ArgParser.parsers[URL::class.java] = UrlParser()

        return this
    }

    /**
     * Builds a new CommandClient instance
     *
     * @return a CommandClient instance
     */
    fun build(): Flight {
        val resources = FlightResources(
            prefixes = prefixProvider
                ?: DefaultPrefixProvider(prefixes, allowMentionPrefix),
            ratelimits = ratelimitProvider
                ?: DefaultRateLimitStrategy(),
            eventFlow = eventFlow
                ?: MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),
            dispatcher = dispatcher
                ?: Executors.newFixedThreadPool(commandThreads).asCoroutineDispatcher(),
            ignoreBots = ignoreBots,
            doTyping = doTyping,
            inhibitor = inhibitor,
            developers = developers
        )

        return Flight(resources)
    }
}
