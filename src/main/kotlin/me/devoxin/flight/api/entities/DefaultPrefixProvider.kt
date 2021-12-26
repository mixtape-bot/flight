package me.devoxin.flight.api.entities

import net.dv8tion.jda.api.entities.Message

public class DefaultPrefixProvider(private val prefixes: List<String>, private val allowMentionPrefix: Boolean) : PrefixProvider {
    override suspend fun provide(message: Message): List<String> {
        val prefixes = mutableListOf<String>()
        if (allowMentionPrefix) {
            val selfUserId = message.jda.selfUser.id
            prefixes.add("<@$selfUserId> ")
            prefixes.add("<@!$selfUserId> ")
        }

        prefixes.addAll(this.prefixes)
        return prefixes
    }
}
