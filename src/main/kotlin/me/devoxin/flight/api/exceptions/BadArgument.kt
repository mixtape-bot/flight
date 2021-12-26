package me.devoxin.flight.api.exceptions

import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.internal.arguments.Argument

public class BadArgument(
    public val argument: Argument,
    public val providedArgument: String,
    public val original: Throwable? = null
) : Throwable("`${argument.name}` must be a ${if (argument.greedy?.type == Greedy.Type.Normal) "list of" else ""} `${argument.type.simpleName}`")
