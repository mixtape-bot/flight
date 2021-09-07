package me.devoxin.flight.api.command.slash.annotations

import me.devoxin.flight.annotation.FlightPreview
import net.dv8tion.jda.api.Permission

/**
 * Marks a function as a slash command. This should be used to annotate methods within a cog as commands,
 * so that Flight can detect them, and register them.
 */
@FlightPreview
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class SlashCommand(
    val name: String = "",
    val description: String = "No description provided",
    val enabled: Boolean = true,
    val privileges: Array<Privilege> = [],
    val hidden: Boolean = false,
    /* helpful things so that this is actually usable. */
    val developerOnly: Boolean = false,
    val guildOnly: Boolean = false,
    val nsfwOnly: Boolean = false,
    val userPermissions: Array<Permission> = [],
    val botPermissions: Array<Permission> = [],
    val guildId: Long = -1L
)
