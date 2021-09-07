package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*
import javax.naming.OperationNotSupportedException

interface Resolver<T> {
    val optionType: OptionType?
        get() = null

    suspend fun getChoiceValue(name: String, value: Any): Command.Choice =
        throw OperationNotSupportedException()

    suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<T> =
        throw OperationNotSupportedException()

    suspend fun parseContent(ctx: MessageContext, param: String): Optional<T>
}
