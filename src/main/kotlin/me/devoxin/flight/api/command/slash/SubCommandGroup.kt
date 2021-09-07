package me.devoxin.flight.api.command.slash

import me.devoxin.flight.annotation.FlightPreview

/**
 * Represents a sub-command group.
 */
@FlightPreview
data class SubCommandGroup(
    val name: String,
    val description: String = "No description provided",
    val commands: List<SlashSubCommandFunction>
) {
    val commandMap: Map<String, SlashSubCommandFunction>
        get() = commands.associateBy { it.name }
}
