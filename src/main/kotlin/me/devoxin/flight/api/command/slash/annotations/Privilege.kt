package me.devoxin.flight.api.command.slash.annotations

import me.devoxin.flight.annotation.FlightPreview
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege

/**
 * Adds this privilege to the function's relating command.
 * This annotation is repeatable.
 */
@FlightPreview
annotation class Privilege(val id: Long, val type: CommandPrivilege.Type = CommandPrivilege.Type.USER)
