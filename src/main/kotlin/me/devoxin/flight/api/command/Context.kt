package me.devoxin.flight.api.command

import me.devoxin.flight.api.Flight
import me.devoxin.flight.api.entities.Attachment
import me.devoxin.flight.internal.entities.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User

interface Context {
    /**
     * The main flight instance that probably created this context.
     */
    val flight: Flight

    /**
     * The current JDA instance.
     */
    val jda: JDA

    /**
     * The phrase that triggered the [command] to be executed.
     */
    val trigger: String

    /**
     * The prefix the [author] used to execute the [command]
     */
    val prefix: String

    /**
     * The command that was executed.
     */
    val command: ICommand

    /**
     * The user that executed the [command]
     */
    val author: User

    /**
     * The guild that the [command] was executed in, or null if the command was executed in a DM.
     */
    val guild: Guild?

    /**
     * The channel that [command] was executed in.
     */
    val channel: MessageChannel

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param content
     *        The content of the message.
     */
    fun send(content: String)

    /**
     * Sends a file to the channel the Context was created from.
     *
     * @param attachment
     *        The attachment to send.
     */
    fun send(attachment: Attachment)

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param build
     *        Options to apply to the message embed.
     */
    fun send(build: EmbedBuilder.() -> Unit)

    /**
     * Sends the message author a private message.
     *
     * @param content
     *        The content of the message.
     */
    fun sendPrivate(content: String)

    /**
     * Sends a private message embed to the author.
     *
     * @param build
     *        Options to apply to the message embed.
     */
    fun sendPrivate(build: EmbedBuilder.() -> Unit)
}
