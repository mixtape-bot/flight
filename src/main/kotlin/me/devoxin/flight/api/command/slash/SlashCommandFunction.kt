package me.devoxin.flight.api.command.slash

import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.command.slash.annotations.SlashCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import me.devoxin.flight.internal.entities.ICommand
import net.dv8tion.jda.api.interactions.commands.Command
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

@FlightPreview
data class SlashCommandFunction(
    override val name: String,
    override val method: KFunction<*>?,
    override val cog: Cog,
    override val arguments: List<CommandArgument>,
    override val contextParameter: KParameter?,
    override val category: String,
    val properties: SlashCommand,
    val subCommands: List<SlashSubCommandFunction>,
    val subCommandGroups: Map<String, SubCommandGroup>,
) : ICommand.Slash, ICommand.Categorized {
    val subCommandMap: Map<String, SlashSubCommandFunction>
        get() = subCommands.associateBy { it.name }

    /**
     * The command reference.
     */
    var ref: Command? = null
        internal set
}
