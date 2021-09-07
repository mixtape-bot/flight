package me.devoxin.flight.api.command.slash

import me.devoxin.flight.api.Flight
import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.entities.Attachment
import me.devoxin.flight.internal.entities.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.CommandInteraction

class SlashContext(
    val interaction: CommandInteraction,
    override val flight: Flight,
    override val command: ICommand.Slash
) : Context {
    override val jda: JDA
        get() = interaction.jda

    override val author: User
        get() = interaction.user

    override val guild: Guild?
        get() = interaction.guild

    override val channel: MessageChannel
        get() = interaction.channel

    override val prefix: String
        get() = "/"

    override val trigger: String
        get() = command.name

    override val member: Member?
        get() = interaction.member

    override fun send(content: String) {
        interaction
            .reply(content)
            .queue()
    }

    override fun send(attachment: Attachment) {
        interaction
            .deferReply()
            .addFile(attachment.stream, attachment.filename)
            .queue()

//        throw OperationNotSupportedException("Unable to send attachments through interactions.")
    }

    override fun send(build: EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
            .apply(build)
            .build()

        interaction.replyEmbeds(embed).queue()
    }

    override fun sendPrivate(content: String) {
        interaction.deferReply(true)
            .setContent(content)
            .queue()
    }

    override fun sendPrivate(build: EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
            .apply(build)
            .build()

        interaction.deferReply(true)
            .addEmbeds(embed)
            .queue()
    }
}
