package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import me.devoxin.flight.internal.arguments.CommandArgument
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.*
import javax.naming.OperationNotSupportedException

interface Resolver<T> {
    val optionType: OptionType?
        get() = null

    suspend fun getOption(argument: CommandArgument): OptionData {
        optionType ?: throw OperationNotSupportedException()
        val description = argument.optionProperties?.description!!
        return OptionData(optionType!!, argument.name, description, argument.isRequired)
    }

    suspend fun getOptionChoice(name: String, value: Any): Command.Choice =
        throw OperationNotSupportedException()

    suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<T> =
        throw OperationNotSupportedException()

    suspend fun resolve(ctx: MessageContext, param: String): Optional<T>
}
