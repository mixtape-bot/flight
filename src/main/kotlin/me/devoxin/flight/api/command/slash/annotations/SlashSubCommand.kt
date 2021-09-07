package me.devoxin.flight.api.command.slash.annotations

import me.devoxin.flight.annotation.FlightPreview
import net.dv8tion.jda.api.Permission

/**
 * Marks a function as a slash sub-command.
 *
 * Sub-commands cannot co-exist with base commands (marked with @SlashCommand).
 * If a cog contains multiple parent commands, and any sub commands, an exception will be thrown.
 *
 * Ideally, commands that have sub commands should be separated into their own cogs.
 */
@FlightPreview
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SlashSubCommand(
    val description: String = "No description available.",
    /* useful things that will make this actually usable. */
    val userPermissions: Array<Permission> = [],
    val botPermissions: Array<Permission> = []
)
