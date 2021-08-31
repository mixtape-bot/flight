package me.devoxin.flight.internal.arguments.types

import net.dv8tion.jda.api.entities.IMentionable

data class Mentionable(val value: IMentionable) : IMentionable by value
