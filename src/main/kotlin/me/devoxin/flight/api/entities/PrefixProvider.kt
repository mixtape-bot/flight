package me.devoxin.flight.api.entities

import net.dv8tion.jda.api.entities.Message

fun interface PrefixProvider {
    suspend fun provide(message: Message): List<String>
}
