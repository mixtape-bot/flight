package me.devoxin.flight.api

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import me.devoxin.flight.api.events.*
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.ratelimit.RateLimit
import me.devoxin.flight.internal.arguments.ArgParser
import me.devoxin.flight.internal.entities.CommandRegistry
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter

class Flight(val resources: FlightResources) : EventListener, CoroutineScope {

    /**
     * All registered commands.
     */
    val commands = CommandRegistry()

    /**
     * Events emitted by this [Flight] instance.
     */
    val events: SharedFlow<Event>
        get() = resources.eventFlow

    override val coroutineContext: CoroutineContext
        get() = resources.dispatcher + SupervisorJob()

    suspend fun handleCommand(ctx: Context, command: CommandFunction) {
        /* handle rate-limits pt1. */
        if (command.rateLimit != null) {
            val entityId = RateLimit.getEntityId(command.rateLimit, ctx)
            if (entityId != null) {
                if (resources.ratelimits.isRateLimited(entityId, command.rateLimit.type, command.name)) {
                    val time = resources.ratelimits.getExpirationDate(entityId, command.rateLimit.type, command.name)
                    return emit(CommandRateLimitedEvent(ctx, command, time, entityId))
                }
            }
        }

        /* do some checks. */
        val props = command.properties

        // developer only
        if (props.developerOnly && !resources.developers.contains(ctx.author.idLong)) {
            return
        }

        // guild only
        if (!ctx.message.channelType.isGuild && props.guildOnly) {
            return
        }

        // permissions
        if (ctx.message.channelType.isGuild) {
            /* check for missing user permissions */
            val userPerms = if (ctx.command is SubCommandFunction)
                mergePermissions(ctx.command.properties.userPermissions, props.userPermissions) else
                props.userPermissions

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
                mergePermissions(ctx.command.properties.botPermissions, props.botPermissions) else
                props.botPermissions

            // TODO: clean up ^^ and the user permissions

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
                return
            }
        }

        emit(CommandInvokedEvent(ctx, command))

        /* run inhibitors */
        val shouldExecute = resources.inhibitor(ctx, command)
            && command.cog.localCheck(ctx, command)

        if (!shouldExecute) {
            return
        }

        /* parse arguments */
        val arguments: HashMap<KParameter, Any?>

        try {
            arguments = ArgParser.parseArguments(ctx.command, ctx, command.properties.argDelimiter)
        } catch (e: BadArgument) {
            return emit(BadArgumentEvent(ctx, command, e))
        } catch (e: Throwable) {
            return emit(ParsingErrorEvent(ctx, command, e))
        }

        /* handle rate-limits pt2 */
        if ((command.rateLimit != null && command.rateLimit.duration > 0) && !resources.developers.contains(ctx.author.idLong)) {
            val entityId = RateLimit.getEntityId(command.rateLimit, ctx)
            if (entityId != null) {
                val duration = command.rateLimit.durationUnit.toMillis(command.rateLimit.duration)
                resources.ratelimits.putRateLimit(entityId, command.rateLimit.type, duration, command.name)
            }
        }

        /* execute the command. */
        val exec = suspend {
            val exception = ctx.command.execute(ctx, arguments)
                .exceptionOrNull()

            if (exception != null) {
                val handled = command.cog.onCommandError(ctx, command, exception)
                if (!handled) {
                    emit(CommandFailedEvent(ctx, command, exception))
                }
            }

            emit(CommandExecutedEvent(ctx, command, exception != null))
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
        val prefixes = resources.prefixes.provide(message)
        val prefix = prefixes.firstOrNull { message.contentRaw.startsWith(it) } // This will break for "?", "??", "???"
                ?: return

        if (prefix.length == message.contentRaw.length) {
            return
        }

        /* get the supplied arguments. */
        val content = message.contentRaw.drop(prefix.length)

        /* find the command trigger. */
        val trigger = commands.values.firstNotNullOf { command ->
            val triggers = listOf(command.name, *command.properties.aliases)
            val pattern = """(?i)^(${triggers.joinToString("|") { "\\Q$it\\E" }})($|\s.+$)""".toPattern()
            pattern.matcher(content).takeIf { it.find() }?.group(1)
        }.lowercase()

        /* get the remaining arguments. */
        val args = content
            .drop(trigger.length)
            .trim()
            .split(" +".toRegex())
            .toMutableList()

        /* find the root command. */
        val command = commands[trigger]
            ?: commands.findCommandByAlias(trigger)
            ?: return emit(UnknownCommandEvent(message, trigger, args))

        /* find a sub command. */
        val subcommand = args.firstOrNull()?.let { command.subcommands[it.lowercase()] }
        if (subcommand != null) {
            args.removeAt(0)
        }

        val invoked = subcommand ?: command

        /* handle the command. */
        val ctx = Context(this, message, prefix, invoked, prefix, args)
        resources.dispatcher.invoke {
            handleCommand(ctx, command)
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

    private fun mergePermissions(a: Array<Permission>, b: Array<Permission>): Array<Permission> {
        val new = arrayListOf<Permission>()
        new.addAll(a)
        new.addAll(b)

        return new.toTypedArray() // hmm
    }

    private fun onReady(event: ReadyEvent) {
        if (resources.developers.isEmpty()) {
            event.jda.retrieveApplicationInfo().queue {
                resources.developers.add(it.owner.idLong)
            }
        }
    }

    private suspend fun emit(event: Event) {
        val err = event.runCatching { resources.eventFlow.emit(this) }.exceptionOrNull()
            ?: return

        try {
            resources.eventFlow.emit(FlightExceptionEvent(err))
        } catch (ex: Exception) {
            log.error("An uncaught error occurred while event dispatch!", ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Flight::class.java)
    }
}
