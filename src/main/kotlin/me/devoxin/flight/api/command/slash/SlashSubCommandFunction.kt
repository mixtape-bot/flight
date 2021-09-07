package me.devoxin.flight.api.command.slash

import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.command.slash.annotations.SlashSubCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import me.devoxin.flight.internal.entities.ICommand
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

@FlightPreview
data class SlashSubCommandFunction(
    override val name: String,
    override val method: KFunction<*>,
    override val cog: Cog,
    override val arguments: List<CommandArgument>,
    override val contextParameter: KParameter,
    override val declaringClass: Any = cog,
    val properties: SlashSubCommand
) : ICommand.Slash
