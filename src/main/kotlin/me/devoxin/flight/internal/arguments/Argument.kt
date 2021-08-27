package me.devoxin.flight.internal.arguments

import me.devoxin.flight.api.annotations.GreedyInfo
import kotlin.reflect.KParameter

class Argument(
    val name: String,
    val type: Class<*>,
    val greedy: GreedyInfo?,
    val optional: Boolean, // Denotes that a parameter has a default value.
    val isNullable: Boolean,
    val isTentative: Boolean,
    internal val kparam: KParameter
) {

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

}
