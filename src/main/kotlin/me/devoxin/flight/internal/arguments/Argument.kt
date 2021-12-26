package me.devoxin.flight.internal.arguments

import me.devoxin.flight.api.annotations.Greedy
import kotlin.reflect.KParameter

public class Argument(
    public val name: String,
    public val type: Class<*>,
    public val greedy: Greedy?,
    public val optional: Boolean, // Denotes that a parameter has a default value.
    public val isNullable: Boolean,
    public val isTentative: Boolean,
    internal val kparam: KParameter
) {
    public fun format(withType: Boolean): String {
        return buildString {
            if (optional || isNullable) {
                append('[')
            } else {
                append('<')
            }

            if (greedy?.type == Greedy.Type.Normal) {
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
