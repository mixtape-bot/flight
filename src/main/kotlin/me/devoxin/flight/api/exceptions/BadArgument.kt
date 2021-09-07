package me.devoxin.flight.api.exceptions

import me.devoxin.flight.internal.arguments.CommandArgument

class BadArgument(
    val argument: CommandArgument,
    val providedArgument: String?,
    val original: Throwable? = null
) : Throwable("`${argument.name}` must be a `${argument.type.simpleName}`")
