package me.devoxin.flight.api.annotations

import net.dv8tion.jda.api.Permission

/**
 * Marks a function as subcommand.
 *
 * Subcommands cannot co-exist with multiple parent commands (marked with @Command).
 * If a cog contains multiple parent commands, and any subcommands, an exception will be thrown.
 *
 * Ideally, commands that have subcommands should be separated into their own cogs.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
public annotation class SubCommand(
    val aliases: Array<String> = [],
    val description: String = "No description available",
    val botPermissions: Array<Permission> = [],
    val userPermissions: Array<Permission> = [],
    val developerOnly: Boolean = false
)
