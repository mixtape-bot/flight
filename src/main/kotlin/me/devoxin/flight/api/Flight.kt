package me.devoxin.flight.api

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import me.devoxin.flight.api.events.*
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.ratelimit.Ratelimit
import me.devoxin.flight.internal.arguments.ArgParser
import me.devoxin.flight.internal.entities.CommandRegistry
import me.devoxin.flight.internal.utils.unwrap
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter

public class Flight internal constructor(public val resources: FlightResources) : EventListener, CoroutineScope {
    public companion object {
        private val log = KotlinLogging.logger {  }
    }

    /**
     * All registered commands.
     */
    public val commands: CommandRegistry = CommandRegistry()

    /**
     * Events emitted by this [Flight] instance.
     */
    public val events: SharedFlow<Event>
        get() = resources.eventFlow

    override val coroutineContext: CoroutineContext
        get() = resources.dispatcher + SupervisorJob() + CoroutineName("Flight")

    public suspend fun handleCommand(ctx: Context, command: CommandFunction) {
        /* handle rate-limits pt1. */
        if (command.rateLimit != null) {
            val entityId = Ratelimit.getEntityId(command.rateLimit, ctx)
            if (entityId != null) {
                if (resources.ratelimitManager.isRatelimited(entityId, command.rateLimit.type, command.name)) {
                    val time = resources.ratelimitManager.getExpirationDate(entityId, command.rateLimit.type, command.name)
                    return emit(CommandRateLimitedEvent(ctx, command, time, entityId))
                }
            }
        }

        /* do some checks. */
        val props = command.properties

        // developer only
        val developerOnly = props.developerOnly.takeIf { it }
            ?: if (ctx.command is SubCommandFunction) ctx.command.properties.developerOnly else false // parent command != true then check sub-command

        if (developerOnly && !resources.developers.contains(ctx.author.idLong)) {
            return emit(BadEnvironmentEvent(ctx, command, BadEnvironmentEvent.Reason.NonDeveloper))
        }

        // guild only
        if (!ctx.message.channelType.isGuild && props.guildOnly) {
            return emit(BadEnvironmentEvent(ctx, command, BadEnvironmentEvent.Reason.NonGuild))
        }

        // permissions
        if (ctx.message.channelType.isGuild) {
            /* check for missing user permissions */
            val userPerms = if (ctx.command is SubCommandFunction)
                setOf(*ctx.command.properties.userPermissions, *props.userPermissions) else
                props.userPermissions.toSet()

            if (userPerms.isNotEmpty()) {
                val missing = userPerms.filterNot {
                    ctx.message.member!!.hasPermission(ctx.textChannel!!, it)
                }

                if (missing.isNotEmpty()) {
                    return emit(UserMissingPermissionsEvent(ctx, command, missing))
                }
            }

            /* check for missing bot permissions */
            val botPerms = if (ctx.command is SubCommandFunction)
                setOf(*ctx.command.properties.botPermissions, *props.botPermissions) else
                props.botPermissions.toSet()

            if (botPerms.isNotEmpty()) {
                val missing = botPerms.filterNot {
                    ctx.guild!!.selfMember.hasPermission(ctx.textChannel!!, it)
                }

                if (missing.isNotEmpty()) {
                    return emit(MissingPermissionsEvent(ctx, command, missing))
                }
            }

            /* NSFW channel check */
            if (props.nsfw && !ctx.textChannel!!.isNSFW) {
                return emit(BadEnvironmentEvent(ctx, command, BadEnvironmentEvent.Reason.NonNSFW))
            }
        }

        emit(CommandInvokedEvent(ctx, command))

        /* run inhibitors */
        val shouldExecute = resources.inhibitor.run(ctx, command) && command.cog.localCheck(ctx, command)
        if (!shouldExecute) {
            return
        }

        /* parse arguments */
        val arguments: MutableMap<KParameter, Any?>

        try {
            arguments = ArgParser.parseArguments(ctx, ctx.command, command.properties.argDelimiter)
        } catch (e: BadArgument) {
            return emit(BadArgumentEvent(ctx, command, e))
        } catch (e: Throwable) {
            return emit(ParsingErrorEvent(ctx, command, e))
        }

        /* handle rate-limits pt2 */
        if ((command.rateLimit != null && command.rateLimit.duration > 0) && !resources.developers.contains(ctx.author.idLong)) {
            val entityId = Ratelimit.getEntityId(command.rateLimit, ctx)
            if (entityId != null) {
                val duration = command.rateLimit.durationUnit.toMillis(command.rateLimit.duration)
                resources.ratelimitManager.putRatelimit(entityId, command.rateLimit.type, duration, command.name)
            }
        }

        /* execute the command. */
        val exec = suspend {
            val exception = ctx.command.execute(ctx, arguments)
            if (exception.isNotEmpty()) {
                val handled = command.cog.onCommandError(ctx, command, exception.unwrap())
                if (!handled) {
                    emit(CommandFailedEvent(ctx, command, exception.unwrap()))
                }
            }

            emit(CommandExecutedEvent(ctx, command, exception.isNotEmpty()))
        }

        if (resources.doTyping) {
            ctx.typingAsync(exec)
        } else {
            exec()
        }
    }

    private suspend fun handleMessage(message: Message) {
        if (resources.ignoreBots && (message.author.isBot || message.isWebhookMessage)) {
            return
        }

        /* get the used prefix. */
        val prefixes = resources.prefixProvider.provide(message)
        val prefix = prefixes.firstOrNull { message.contentRaw.startsWith(it) } // This will break for "?", "??", "???"
            ?: return

        if (prefix.length == message.contentRaw.length) {
            return
        }

        /* get the supplied arguments. */
        var content = message.contentRaw.drop(prefix.length).trim()

        /* find the command trigger. */
        val root = Commands.findCommand(commands, content)
        if (root == null) {
            val args = content.split(" +".toRegex())
            return emit(UnknownCommandEvent(message, args.first(), args.drop(1)))
        }

        /* get the remaining arguments. */
        content = content
            .drop(root.trigger.length)
            .trim()

        /* find a sub command. */
        val fsc = content
            .ifBlank { null }
            ?.let { Commands.findSubCommand(root.command, it) }

        if (fsc != null) {
            /* found one */
            content = content.drop(fsc.trigger.length).trim()
        }

        /* handle the command. */
        val ctx = Context(
            flight = this@Flight,
            message = message,
            trigger = "${root.trigger}${fsc?.trigger ?: ""}",
            command = fsc?.command ?: root.command,
            prefix = prefix,
            args = content.split(" +".toRegex())
        )

        launch {
            handleCommand(ctx, root.command)
        }
    }

    // +-------------------+
    // | Execution-Related |
    // +-------------------+
    override fun onEvent(event: GenericEvent) {
        launch {
            try {
                when (event) {
                    is ReadyEvent ->
                        onReady(event)

                    is MessageReceivedEvent ->
                        handleMessage(event.message)
                }
            } catch (ex: Throwable) {
                emit(FlightExceptionEvent(ex))
            }
        }
    }

    private fun onReady(event: ReadyEvent) {
        if (resources.developers.isEmpty()) {
            event.jda
                .retrieveApplicationInfo()
                .queue { resources.developers.add(it.owner.idLong) }
        }
    }

    private suspend fun emit(event: Event) {
        val err = event
            .runCatching { resources.eventFlow.emit(this) }
            .exceptionOrNull()
            ?: return

        try {
            resources.eventFlow.emit(FlightExceptionEvent(err))
        } catch (ex: Exception) {
            log.error(ex) { "An uncaught error occurred while event dispatch!" }
        }
    }
}
