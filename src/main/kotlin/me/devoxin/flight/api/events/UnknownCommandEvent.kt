package me.devoxin.flight.api.events

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteraction

/**
 * Emitted when a command was not found for the input provided by the User.
 * This will be emitted upon successful prefix match, but unsuccessful command label match
 *
 * @param message
 *   The message that was received.
 *
 * @param command
 *   The command label that the user provided
 *
 * @param args
 *   Any additional arguments provided by the user.
 */
data class UnknownCommandEvent(
    val message: Message,
    val command: String,
    val args: List<String>
) : Event {
    /**
     * The user that tried invoking an unknown command.
     */
    val user: User
        get() = message.author
}

/**
 * Emitted when a slash command was not found for the input provided by the User.
 *
 * @param interaction
 *   The message that was received.
 *
 * @param command
 *   The command label that the user provided
 */
data class UnknownSlashCommandEvent(
    val interaction: CommandInteraction,
    val command: String
) : Event {
    /**
     * The user that tried invoking an unknown command.
     */
    val user: User
        get() = interaction.user
}

/**
 * Emitted when a slash sub-command was not found for the input provided by the User.
 *
 * @param interaction
 *   The message that was received.
 *
 * @param command
 *   The command label that the user provided
 */
data class UnknownSlashSubCommandEvent(
    val interaction: CommandInteraction,
    val command: String
) : Event {
    /**
     * The user that tried invoking an unknown command.
     */
    val user: User
        get() = interaction.user
}

/**
 * Emitted when a slash sub-command group was not found.
 *
 * @param interaction
 *   The message that was received.
 *
 * @param group
 *   The sub-command group the user provided
 */
data class UnknownSlashSubCommandGroupEvent(
    val interaction: CommandInteraction,
    val group: String
) : Event {
    /**
     * The user that tried invoking an unknown command.
     */
    val user: User
        get() = interaction.user
}
