package me.devoxin.flight.api.command.slash.annotations

import me.devoxin.flight.annotation.FlightPreview

/**
 * Marks a cog as a sub-command holder, this cog may only have sub-commands and nested sub-command holders
 */
@FlightPreview
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SubCommandHolder(val name: String, val description: String = "No description supplied.")
