package me.devoxin.flight.api.entities

import net.dv8tion.jda.api.entities.Message

public fun interface PrefixProvider {
    /**
     * @param message
     *   The message.
     *
     * @return A list of acceptable prefixes.
     */
    public suspend fun provide(message: Message): List<String>
}
