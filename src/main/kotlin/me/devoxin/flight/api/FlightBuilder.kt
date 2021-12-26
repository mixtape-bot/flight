package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import me.devoxin.flight.api.entities.*
import me.devoxin.flight.api.events.Event
import me.devoxin.flight.api.ratelimit.RatelimitManager
import me.devoxin.flight.api.ratelimit.DefaultRatelimitManager
import me.devoxin.flight.internal.arguments.ArgParser
import me.devoxin.flight.internal.arguments.resolvers.*
import me.devoxin.flight.internal.arguments.resolvers.jda.*
import me.devoxin.flight.internal.arguments.resolvers.kotlin.*
import java.util.concurrent.Executors

public class FlightBuilder {
    /**
     * Strings that messages must start with to trigger the bot.
     */
    public var prefixes: List<String> = emptyList()

    /**
     * Whether the bot will allow mentions to be used as a prefix.
     */
    public var allowMentionPrefix: Boolean = true

    /**
     * Whether bots and webhooks should be ignored. The recommended option is true to prevent feedback loops.
     */
    public var ignoreBots: Boolean = true

    /**
     * The provider used for obtaining prefixes
     */
    public var prefixProvider: PrefixProvider? = null

    /**
     * The provider used for rate-limits.
     */
    public var ratelimitManager: RatelimitManager? = null

    /**
     * The coroutine dispatcher used for executing commands.
     */
    public var dispatcher: CoroutineDispatcher? = null

    /**
     * The number of threads used for the default [dispatcher]
     */
    public var commandThreads: Int = Runtime.getRuntime().availableProcessors() * 2

    /**
     * Determines whether a command should be executed or not.
     */
    public var inhibitor: Inhibitor = Inhibitor { _, _ -> true }

    /**
     * Used to emit several [Event]s
     */
    public var eventFlow: MutableSharedFlow<Event>? = null

    /**
     * Whether to send typing events during command execution.
     */
    public var doTyping: Boolean = false

    /**
     * A list of IDs as the developers. Any users with the given IDs
     * are then able to use commands marked with `developerOnly`.
     */
    public var developers: MutableSet<Long> = mutableSetOf()

    /**
     * Registers an argument resolver to the given class.
     *
     * @return The builder instance. Useful for chaining.
     */
    public fun <T> resolver(klass: Class<T>, resolver: Resolver<T>): FlightBuilder {
        // This is kinda unsafe. Would use T, but nullable/boxed types revert
        // to their java.lang counterparts. E.g. Int? becomes java.lang.Integer,
        // but Int remains kotlin.Int.
        // See https://youtrack.jetbrains.com/issue/KT-35423

        ArgParser.useResolver(klass, resolver)
        return this
    }

    /**
     * Registers an argument resolver for the given class.
     *
     * @param resolver The resolver to register.
     */
    public inline fun <reified T> resolver(resolver: Resolver<T>): FlightBuilder =
        resolver(T::class.java, resolver)

    /**
     * Registers all default argument resolvers.
     *
     * @return The builder instance. Useful for chaining.
     */
    public fun registerDefaultResolvers(): FlightBuilder {
        /* Kotlin types and primitives */
        val booleanResolver = BooleanResolver()
        ArgParser.resolvers[Boolean::class.java] = booleanResolver
        ArgParser.resolvers[java.lang.Boolean::class.java] = booleanResolver

        val doubleResolver = DoubleResolver()
        ArgParser.resolvers[Double::class.java] = doubleResolver
        ArgParser.resolvers[java.lang.Double::class.java] = doubleResolver

        val floatResolver = FloatResolver()
        ArgParser.resolvers[Float::class.java] = floatResolver
        ArgParser.resolvers[java.lang.Float::class.java] = floatResolver

        val intResolver = IntResolver()
        ArgParser.resolvers[Int::class.java] = intResolver
        ArgParser.resolvers[java.lang.Integer::class.java] = intResolver

        val longResolver = LongResolver()
        ArgParser.resolvers[Long::class.java] = longResolver
        ArgParser.resolvers[java.lang.Long::class.java] = longResolver

        val stringResolver = StringResolver()
        ArgParser.resolvers[String::class.java] = stringResolver
        ArgParser.resolvers[java.lang.String::class.java] = stringResolver

        /* JDA entities */
        ArgParser.useResolver(MemberResolver)
        ArgParser.useResolver(RoleResolver)
        ArgParser.useResolver(UserResolver())
        ArgParser.useResolver(InviteResolver())
        ArgParser.useResolver(TextChannelResolver())
        ArgParser.useResolver(VoiceChannelResolver())

        /* Custom entities */
        ArgParser.useResolver(SnowflakeResolver)
        ArgParser.useResolver(EmojiResolver())
        ArgParser.useResolver(MentionableResolver())

        /* java tings */
        ArgParser.useResolver(UrlResolver())

        return this
    }

    /**
     * Builds a new CommandClient instance
     *
     * @return a CommandClient instance
     */
    public fun build(): Flight {
        val resources = FlightResources(
            prefixProvider = prefixProvider ?: DefaultPrefixProvider(prefixes, allowMentionPrefix),
            ratelimitManager = ratelimitManager ?: DefaultRatelimitManager(),
            eventFlow = eventFlow ?: MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE),
            dispatcher = dispatcher ?: Executors.newFixedThreadPool(commandThreads).asCoroutineDispatcher(),
            ignoreBots = ignoreBots,
            doTyping = doTyping,
            inhibitor = inhibitor,
            developers = developers
        )

        return Flight(resources)
    }
}
