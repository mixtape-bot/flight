package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*
import javax.naming.OperationNotSupportedException

class BooleanResolver : Resolver<Boolean> {
    override val optionType: OptionType = OptionType.BOOLEAN

    override suspend fun getChoiceValue(name: String, value: Any): Command.Choice =
        throw OperationNotSupportedException()

    override suspend fun getOptionValue(ctx: SlashContext, option: OptionMapping): Optional<Boolean> =
        Optional.of(option.asBoolean)

    override suspend fun parseContent(ctx: MessageContext, param: String): Optional<Boolean> {
        if (trueExpr.contains(param)) {
            return Optional.of(true)
        } else if (falseExpr.contains(param)) {
            return Optional.of(false)
        }

        return Optional.empty()
    }

    companion object {
        val trueExpr = listOf("yes", "y", "true", "t", "1", "enable", "on")
        val falseExpr = listOf("no", "n", "false", "f", "0", "disable", "off")
    }

}
