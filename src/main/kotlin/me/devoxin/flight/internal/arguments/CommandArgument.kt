package me.devoxin.flight.internal.arguments

import me.devoxin.flight.api.command.message.annotations.GreedyInfo
import me.devoxin.flight.api.exceptions.ParserNotRegistered
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KParameter

class CommandArgument(
    val name: String,
    val type: Class<*>,
    val greedy: GreedyInfo?,
    val optional: Boolean, // Denotes that a parameter has a default value.
    val isNullable: Boolean,
    val isTentative: Boolean,
    val optionProperties: Option?,
    internal val kparam: KParameter
) {
    val isRequired: Boolean
        get() = !isNullable || !optional

    val optionType: OptionType?
        get() = ArgParser.parsers[type]?.optionType

    fun format(withType: Boolean): String {
        return buildString {
            if (optional || isNullable) {
                append('[')
            } else {
                append('<')
            }

            if (greedy != null) {
                append("...")
            }
            append(name)

            if (withType) {
                append(": ")
                append(type.simpleName)
            }

            if (optional || isNullable) {
                append(']')
            } else {
                append('>')
            }
        }
    }

    data class Option(val description: String)
}
