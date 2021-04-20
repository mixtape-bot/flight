package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import me.devoxin.flight.api.entities.BucketType
import me.devoxin.flight.api.entities.CooldownProvider
import me.devoxin.flight.api.entities.PrefixProvider
import me.devoxin.flight.api.events.*
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.internal.arguments.ArgParser
import me.devoxin.flight.internal.entities.CommandRegistry
import me.devoxin.flight.internal.entities.ExecutionCallback
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter

class CommandClient(
  private val prefixProvider: PrefixProvider,
  private val cooldownProvider: CooldownProvider,
  private val ignoreBots: Boolean,
  private val coroutineDispatcher: CoroutineDispatcher,
  private val eventFlow: MutableSharedFlow<Event>,
  private val doTyping: Boolean,
  val inhibitor: suspend (Context, CommandFunction) -> Boolean,
  customOwnerIds: MutableSet<Long>
) : EventListener, CoroutineScope {
  /**
   * IDs of Users to treat as "Owners"
   */
  val ownerIds = customOwnerIds

  /**
   * All registered commands.
   */
  val commands = CommandRegistry()

  /**
   * Events emitted by this [CommandClient]
   */
  val events: SharedFlow<Event>
    get() = eventFlow

  override val coroutineContext: CoroutineContext
    get() = coroutineDispatcher + SupervisorJob()

  private suspend fun onMessageReceived(event: MessageReceivedEvent) {
    if (ignoreBots && (event.author.isBot || event.isWebhookMessage)) {
      return
    }

    val prefixes = prefixProvider.provide(event.message)
    val prefix =
      prefixes.firstOrNull { event.message.contentRaw.startsWith(it) } // This will break for "?", "??", "???"
        ?: return

    if (prefix.length == event.message.contentRaw.length) {
      return
    }

    val args = event.message.contentRaw.take(prefix.length).split(" +".toRegex()).toMutableList()
    val trigger = args.removeFirst().toLowerCase()

    val command = commands[trigger]
      ?: commands.values.firstOrNull { it.properties.aliases.contains(trigger) }
      ?: return emit(UnknownCommandEvent(event, trigger, args))

    val subcommand = args.firstOrNull()?.let { command.subcommands[it.toLowerCase()] }
    val invoked = subcommand ?: command

    if (subcommand != null) {
      args.removeAt(0)
    }

    val ctx = Context(this, event, prefix, invoked)

    if (command.cooldown != null) {
      val entityId = when (command.cooldown.bucket) {
        BucketType.USER -> ctx.author.idLong
        BucketType.GUILD -> ctx.guild?.idLong
        BucketType.GLOBAL -> -1
      }

      if (entityId != null) {
        if (cooldownProvider.isOnCooldown(entityId, command.cooldown.bucket, command)) {
          val time = cooldownProvider.getCooldownTime(entityId, command.cooldown.bucket, command)
          return emit(CommandRateLimitedEvent(ctx, command, time, entityId))
        }
      }
    }

    val props = command.properties

    if (props.developerOnly && !ownerIds.contains(event.author.idLong)) {
      return
    }

    if (!event.channelType.isGuild && props.guildOnly) {
      return
    }

    // TODO: More events for failed checks

    if (event.channelType.isGuild) {
      /* check for missing user permissions */
      if (props.userPermissions.isNotEmpty()) {
        val missing = props.userPermissions.filterNot { event.member!!.hasPermission(event.textChannel, it) }
        if (missing.isNotEmpty()) {
          return emit(UserMissingPermissionsEvent(ctx, command, missing))
        }
      }

      /* check for missing bot permissions */
      if (props.botPermissions.isNotEmpty()) {
        val missing = props.botPermissions.filterNot { event.guild.selfMember.hasPermission(event.textChannel, it) }
        if (missing.isNotEmpty()) {
          return emit(MissingPermissionsEvent(ctx, command, missing))
        }
      }

      /* NSFW channel check */
      if (props.nsfw && !event.textChannel.isNSFW) {
        return
      }
    }

    emit(CommandInvokedEvent(ctx, command))

    /* run inhibitors */
    val shouldExecute = inhibitor(ctx, command)
      && command.cog.localCheck(ctx, command)

    if (!shouldExecute) {
      return
    }

    /* parse events */
    val arguments: HashMap<KParameter, Any?>

    try {
      arguments = ArgParser.parseArguments(invoked, ctx, args, command.properties.argDelimiter)
    } catch (e: BadArgument) {
      return emit(BadArgumentEvent(ctx, command, e))
    } catch (e: Throwable) {
      return emit(ParsingErrorEvent(ctx, command, e))
    }

    val cb: ExecutionCallback = { success: Boolean, err: Throwable? ->
      if (err != null) {
        val handled = command.cog.onCommandError(ctx, command, err)

        if (!handled) {
          emit(CommandFailedEvent(ctx, command, err))
        }
      }

      emit(CommandExecutedEvent(ctx, command, !success))
    }

    if (command.cooldown != null && command.cooldown.duration > 0) {
      val entityId = when (command.cooldown.bucket) {
        BucketType.USER -> ctx.author.idLong
        BucketType.GUILD -> ctx.guild?.idLong
        BucketType.GLOBAL -> -1
      }

      if (entityId != null) {
        val time = command.cooldown.timeUnit.toMillis(command.cooldown.duration)
        cooldownProvider.setCooldown(entityId, command.cooldown.bucket, time, command)
      }
    }

    val exec = {
      invoked.execute(ctx, arguments, cb, coroutineDispatcher)
    }

    if (doTyping) ctx.typing(exec) else exec()
  }

  // +-------------------+
  // | Execution-Related |
  // +-------------------+
  override fun onEvent(event: GenericEvent) {
    runBlocking(coroutineContext) {
      try {
        when (event) {
          is ReadyEvent ->
            onReady(event)

          is MessageReceivedEvent ->
            onMessageReceived(event)
        }
      } catch (ex: Throwable) {
        emit(FlightExceptionEvent(ex))
      }
    }
  }

  private fun onReady(event: ReadyEvent) {
    if (ownerIds.isEmpty()) {
      event.jda.retrieveApplicationInfo().queue {
        ownerIds.add(it.owner.idLong)
      }
    }
  }

  private suspend fun emit(event: Event) {
    val err = event.runCatching { eventFlow.emit(this) }.exceptionOrNull()
      ?: return

    try {
      eventFlow.emit(FlightExceptionEvent(err))
    } catch (ex: Exception) {
      log.error("An uncaught error occurred while event dispatch!", ex)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CommandClient::class.java)
  }
}
