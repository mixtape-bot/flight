package me.devoxin.flight.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.message.MessageSubCommandFunction
import me.devoxin.flight.api.command.slash.SlashContext
import me.devoxin.flight.api.command.slash.SlashSubCommandFunction
import me.devoxin.flight.api.events.*
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.exceptions.ParserNotRegistered
import me.devoxin.flight.api.ratelimit.RateLimit
import me.devoxin.flight.internal.arguments.ArgParser
import me.devoxin.flight.internal.arguments.CommandArgument
import me.devoxin.flight.internal.entities.CommandRegistry
import me.devoxin.flight.internal.entities.ICommand
import me.devoxin.flight.internal.entities.SlashCommandRegistry
import me.devoxin.flight.internal.entities.execute
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ICommandHolder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter

class Flight(val resources: FlightResources) : EventListener, CoroutineScope {

    /**
     * All registered message commands.
     */
    val messageCommands = CommandRegistry()

    /**
     * All registered slash commands.
     */
    val slashCommands = SlashCommandRegistry()

    /**
     * Events emitted by this [Flight] instance.
     */
    val events: SharedFlow<Event>
        get() = resources.eventFlow

    override val coroutineContext: CoroutineContext
        get() = resources.dispatcher + SupervisorJob()

    suspend fun handleCommand(ctx: MessageContext, commandFunction: MessageCommandFunction) {
        /* handle rate-limits pt1. */
        if (commandFunction.rateLimit != null) {
            val entityId = RateLimit.getEntityId(commandFunction.rateLimit, ctx)
            if (entityId != null) {
                if (resources.ratelimits.isRateLimited(
                        entityId,
                        commandFunction.rateLimit.type,
                        commandFunction.name
                    )
                ) {
                    val time = resources.ratelimits.getExpirationDate(
                        entityId,
                        commandFunction.rateLimit.type,
                        commandFunction.name
                    )
                    return emit(CommandRateLimitedEvent(ctx, commandFunction, time, entityId))
                }
            }
        }

        /* do some checks. */
        val props = commandFunction.properties

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
            val userPerms = if (ctx.command is MessageSubCommandFunction)
                mergePermissions(ctx.command.properties.userPermissions, props.userPermissions) else
                props.userPermissions

            if (userPerms.isNotEmpty()) {
                val missing = userPerms.filterNot {
                    ctx.message.member!!.hasPermission(ctx.textChannel!!, it)
                }

                if (missing.isNotEmpty()) {
                    return emit(UserMissingPermissionsEvent(ctx, commandFunction, missing))
                }
            }

            /* check for missing bot permissions */
            val botPerms = if (ctx.command is MessageSubCommandFunction)
                mergePermissions(ctx.command.properties.botPermissions, props.botPermissions) else
                props.botPermissions

            // TODO: clean up ^^ and the user permissions

            if (botPerms.isNotEmpty()) {
                val missing = botPerms.filterNot {
                    ctx.guild!!.selfMember.hasPermission(ctx.textChannel!!, it)
                }

                if (missing.isNotEmpty()) {
                    return emit(MissingPermissionsEvent(ctx, commandFunction, missing))
                }
            }

            /* NSFW channel check */
            if (props.nsfw && !ctx.textChannel!!.isNSFW) {
                return
            }
        }

        emit(CommandInvokedEvent(ctx, commandFunction))

        /* run inhibitors */
        val shouldExecute = resources.inhibitor(ctx, commandFunction)
            && commandFunction.cog.localCheck(ctx, commandFunction)

        if (!shouldExecute) {
            return
        }

        /* parse arguments */
        val arguments: HashMap<KParameter, Any?>

        try {
            arguments = ArgParser.parseArguments(ctx.command, ctx, commandFunction.properties.argDelimiter)
        } catch (e: BadArgument) {
            return emit(BadArgumentEvent(ctx, commandFunction, e))
        } catch (e: Throwable) {
            return emit(ParsingErrorEvent(ctx, commandFunction, e))
        }

        /* handle rate-limits pt2 */
        if ((commandFunction.rateLimit != null && commandFunction.rateLimit.duration > 0) && !resources.developers.contains(
                ctx.author.idLong
            )
        ) {
            val entityId = RateLimit.getEntityId(commandFunction.rateLimit, ctx)
            if (entityId != null) {
                val duration = commandFunction.rateLimit.durationUnit.toMillis(commandFunction.rateLimit.duration)
                resources.ratelimits.putRateLimit(
                    entityId,
                    commandFunction.rateLimit.type,
                    duration,
                    commandFunction.name
                )
            }
        }

        /* execute the command. */
        val exec = suspend {
            val exception = ctx.command.execute(ctx, arguments)
                .exceptionOrNull()

            if (exception != null) {
                val handled = commandFunction.cog.onCommandError(ctx, commandFunction, exception)
                if (!handled) {
                    emit(CommandFailedEvent(ctx, commandFunction, exception))
                }
            }

            emit(CommandExecutedEvent(ctx, commandFunction, exception != null))
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
        val trigger = messageCommands.values.firstNotNullOf { command ->
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
        val command = messageCommands[trigger]
            ?: messageCommands.findCommandByAlias(trigger)
            ?: return emit(UnknownCommandEvent(message, trigger, args))

        /* find a sub command. */
        val subcommand = args.firstOrNull()?.let { command.subcommands[it.lowercase()] }
        if (subcommand != null) {
            args.removeAt(0)
        }

        val invoked = subcommand ?: command

        /* handle the command. */
        val ctx = MessageContext(this, message, prefix, invoked, prefix, args)
        resources.dispatcher.invoke {
            handleCommand(ctx, command)
        }
    }

    @OptIn(FlightPreview::class)
    private suspend fun handleSlashCommand(interaction: SlashCommandEvent) {
        val baseCommand = slashCommands.findCommandByName(interaction.name)
            ?: return emit(UnknownSlashCommandEvent(interaction, interaction.name))

        var command: ICommand.Slash = baseCommand
        when {
            interaction.subcommandGroup != null -> {
                val group = baseCommand.subCommandGroups[interaction.subcommandGroup]
                    ?: return emit(UnknownSlashSubCommandGroupEvent(interaction, interaction.name))

                command = group.commandMap[interaction.subcommandName!!]
                    ?: return emit(UnknownSlashSubCommandEvent(interaction, interaction.name))
            }

            interaction.subcommandName != null -> {
                command = baseCommand.subCommandMap[interaction.subcommandName!!]
                    ?: return emit(UnknownSlashSubCommandEvent(interaction, interaction.name))
            }
        }

        val ctx = SlashContext(interaction, this, command)
        val arguments = mutableMapOf<KParameter, Any?>()
        try {
            for (argument in command.arguments) {
                val option = interaction
                    .getOptionsByName(argument.name)
                    .firstOrNull()

                if (option == null) {
                    if (argument.isRequired) {
                        throw BadArgument(argument, null, IllegalArgumentException("Not enough arguments"))
                    }

                    arguments[argument.kparam] = null
                    continue
                }

                val parser = ArgParser.parsers[argument.type]
                    ?: throw ParserNotRegistered("No parsers registered for `${argument.type}`")

                val value = parser.getOptionValue(ctx, option)
                if (!value.isPresent && !argument.isRequired) {
                    throw BadArgument(argument, option.asString, IllegalArgumentException("Not enough arguments"))
                }

                arguments[argument.kparam] = value.orElse(null)
            }
        } catch (e: BadArgument) {
            return emit(BadArgumentEvent(ctx, command, e))
        } catch (e: Throwable) {
            return emit(ParsingErrorEvent(ctx, command, e))
        }

        val result = command
            .execute(ctx, arguments)
            .onFailure {
                val handled = command.cog.onCommandError(ctx, command, it)
                if (!handled) {
                    emit(CommandFailedEvent(ctx, command, it))
                }
            }

        emit(CommandExecutedEvent(ctx, command, result.isFailure))
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

                    is SlashCommandEvent ->
                        handleSlashCommand(event)
                }
            } catch (ex: Throwable) {
                emit(FlightExceptionEvent(ex))
            }
        }
    }

    @FlightPreview
    suspend fun syncCommandsForTestGuilds(jda: JDA): Boolean {
        val testGuilds = resources.testGuilds.takeUnless { it.isEmpty() }
            ?: return false

        return testGuilds.any { syncCommands(jda, it, false) }
    }

    @FlightPreview
    suspend fun syncCommands(jda: JDA, guildId: Long? = null, filter: Boolean = true): Boolean {
        val commands = slashCommands.values.filter {
            if (filter) it.properties.guildId.takeUnless { id -> id == -1L } == guildId else true
        }

        if (commands.isEmpty()) {
            return false
        }

        var commandHolder: ICommandHolder = jda
        if (guildId != null) {
            val guild = jda.getGuildById(guildId)
            if (guild == null) {
                log.debug("($guildId) Couldn't sync commands due to it not existing in cache.")
                return false
            }

            log.debug("($guildId) Syncing commands for guild: ${guild.name}")
            commandHolder = guild
        }

        /* get the current commands for this guild. */
        val currentCommands = commandHolder
            .retrieveCommands()
            .submit()
            .await()

        /* find the commands that need to be updated, deleted, or created. */
        val creating = commands.filter { currentCommands.none { c -> c.name == it.name } }
        val deleting = currentCommands.filterNot { commands.any { c -> c.name == it.name } }
        val updating = commands.filter { currentCommands.any { c -> c.name == it.name } }

        log.debug("${"Global commands".takeIf { guildId == null } ?: "($guildId)"} being created=${creating.size}; deleted=${deleting.size}; updated=${updating.size}")

        /* creating */
        val upserting = mutableListOf(*creating.toTypedArray(), *updating.toTypedArray())
        val action = commandHolder.updateCommands()
        for (command in upserting) {
            val data = CommandData(command.name, command.properties.description)
            if (command.subCommands.isNotEmpty()) {
                val subCommands = command.subCommands.map {
                    convertSlashSubCommandToSubcommandData(
                        "slash command \"${command.name}\"",
                        it
                    )
                }
                data.addSubcommands(subCommands)
            } else if (command.subCommandGroups.isNotEmpty()) {
                val groups = command.subCommandGroups.map { (name, group) ->
                    val groupData = SubcommandGroupData(name, group.description)
                    val commands = group.commands.map {
                        convertSlashSubCommandToSubcommandData(
                            "slash sub-command group \"${name}\" of slash command \"${command.name}\"",
                            it
                        )
                    }
                    groupData.addSubcommands(commands)
                }

                data.addSubcommandGroups(groups)
            } else {
                val options = command.arguments.map { convertArgumentToOptionData(command.name, it) }
                data.addOptions(options)
            }

            action.addCommands(data)
        }

        action.queue()
        return true
    }

    @FlightPreview
    private fun convertSlashSubCommandToSubcommandData(
        id: String,
        subCommand: SlashSubCommandFunction
    ): SubcommandData {
        val data = SubcommandData(subCommand.name, subCommand.properties.description)
        val options = subCommand.arguments.map {
            convertArgumentToOptionData(
                "slash sub-command \"${subCommand.name}\" of $id",
                it
            )
        }
        data.addOptions(options)
        return data
    }

    @FlightPreview
    private fun convertArgumentToOptionData(command: String, argument: CommandArgument): OptionData {
        val type = argument.optionType
            ?: throw error("Argument ${argument.name} for $command doesn't have an option type.")

        return OptionData(type, argument.name, argument.optionProperties!!.description, argument.isRequired)
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
