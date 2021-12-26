package me.devoxin.flight.api

import kotlinx.coroutines.future.await
import me.devoxin.flight.api.entities.Attachment
import me.devoxin.flight.internal.entities.Executable
import me.devoxin.flight.internal.utils.Scheduler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import java.util.regex.Pattern

public class Context(
    public val flight: Flight,
    public val message: Message,
    public val trigger: String,
    public val command: Executable,
    public val prefix: String,
    public val args: List<String>
) {
    public companion object {
        private val mentionPattern = Pattern.compile("(?<mention><(?<type>@!?|@&|#!?)(?<id>[0-9]{17,21})>)")
    }

    public val jda: JDA
        get() = message.jda

    public val author: User
        get() = message.author

    public val guild: Guild?
        get() = if (message.isFromGuild) message.guild else null

    public val member: Member?
        get() = message.member

    public val channel: MessageChannel
        get() = message.channel

    public val textChannel: TextChannel?
        get() = channel as? TextChannel

    public val privateChannel: PrivateChannel?
        get() = channel as? PrivateChannel

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param content
     *        The content of the message.
     */
    public fun send(content: String) {
        channel.sendMessage(content).queue()
    }

    /**
     * Sends a file to the channel the Context was created from.
     *
     * @param attachment
     *        The attachment to send.
     */
    public fun send(attachment: Attachment) {
        channel.sendFile(attachment.stream, attachment.filename).queue()
    }

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param build
     *        Options to apply to the message embed.
     */
    public inline fun send(build: EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
            .apply(build)
            .build()

        channel.sendMessageEmbeds(embed).queue()
    }

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param content
     *        The content of the message.
     *
     * @return The created message.
     */
    public suspend fun sendAsync(content: String): Message {
        return channel.sendMessage(content)
            .submit()
            .await()
    }

    /**
     * Sends a file to the channel the Context was created from.
     *
     * @param attachment
     *        The attachment to send.
     *
     * @return The created message.
     */
    public suspend fun sendAsync(attachment: Attachment): Message {
        return channel.sendFile(attachment.stream, attachment.filename)
            .submit()
            .await()
    }

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param build
     *        Options to apply to the message embed.
     *
     * @return The created message.
     */
    public suspend inline fun sendAsync(build: EmbedBuilder.() -> Unit): Message {
        val embed = EmbedBuilder()
            .apply(build)
            .build()

        return channel.sendMessageEmbeds(embed)
            .submit()
            .await()
    }

    /**
     * Sends the message author a direct message.
     *
     * @param content
     *        The content of the message.
     */
    public fun sendPrivate(content: String) {
        author.openPrivateChannel().queue { privateChannel ->
            privateChannel
                .sendMessage(content)
                .queue { privateChannel.close().submit() }
        }
    }

    /**
     * Sends a typing status within the channel until the provided function is exited.
     *
     * @param block
     *        The code that should be executed while the typing status is active.
     */
    public fun typing(block: () -> Unit) {
        channel.sendTyping().queue {
            val task = Scheduler.every(5000) {
                channel.sendTyping().queue()
            }

            block()
            task.cancel(true)
        }
    }

    /**
     * Sends a typing status within the channel until the provided function is exited.
     *
     * @param block
     *        The code that should be executed while the typing status is active.
     */
    public suspend fun typingAsync(block: suspend () -> Unit) {
        channel.sendTyping().submit().await()

        val task = Scheduler.every(5000) { channel.sendTyping().queue() }
        try {
            block()
        } finally {
            task.cancel(true)
        }
    }

    /**
     * Cleans a string, sanitizing all forms of mentions (role, channel and user), replacing them with
     * their display-equivalent where possible (For example, <@123456789123456789> becomes @User).
     *
     * For cases where the mentioned entity is not cached by the bot, the mention will be replaced
     * with @invalid-<entity type>.
     *
     * It's recommended that you use this only for sending responses back to a user.
     *
     * @param str
     *        The string to clean.
     *
     * @returns The sanitized string.
     */
    public fun cleanContent(str: String): String {
        var content = str.replace("e", "е")
        // We use a russian "e" instead of \u200b as it keeps character count the same.
        val matcher = mentionPattern.matcher(str)

        while (matcher.find()) {
            val entityType = matcher.group("type")
            val entityId = matcher.group("id").toLong()
            val fullEntity = matcher.group("mention")

            when (entityType) {
                "@", "@!" -> {
                    val entity = guild?.getMemberById(entityId)?.effectiveName
                        ?: jda.getUserById(entityId)?.name
                        ?: "invalid-user"
                    content = content.replace(fullEntity, "@$entity")
                }

                "@&" -> {
                    val entity = jda.getRoleById(entityId)?.name ?: "invalid-role"
                    content = content.replace(fullEntity, "@$entity")
                }

                "#", "#!" -> {
                    val entity = jda.getTextChannelById(entityId)?.name ?: "invalid-channel"
                    content = content.replace(fullEntity, "#$entity")
                }
            }
        }

        for (emote in message.emotes) {
            content = content.replace(emote.asMention, ":${emote.name}:")
        }

        return content
    }
}
