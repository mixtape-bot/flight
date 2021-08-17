package me.devoxin.flight.api.events

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Emitted when a command was not found for the input provided by the User.
 * This will be emitted upon successful prefix match, but unsuccessful command label match
 *
 * @param event
 *   The message event.
 *
 * @param command
 *   The command label that the user provided
 *
 * @param args
 *   Any additional arguments provided by the user.
 */
class UnknownCommandEvent(
    val event: MessageReceivedEvent,
    val command: String,
    val args: List<String>
) : Event {
    /**
     * The user that tried invoking an unknown command.
     */
    val user: User
        get() = event.author
}
