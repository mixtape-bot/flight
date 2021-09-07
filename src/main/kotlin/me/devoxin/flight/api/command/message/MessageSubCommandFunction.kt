package me.devoxin.flight.api.command.message

import me.devoxin.flight.api.command.message.annotations.MessageSubCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import me.devoxin.flight.internal.entities.ICommand
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

data class MessageSubCommandFunction(
    override val name: String,
    val properties: MessageSubCommand,
    // Executable properties
    override val method: KFunction<*>,
    override val cog: Cog,
    override val arguments: List<CommandArgument>,
    override val contextParameter: KParameter
) : ICommand.Message
