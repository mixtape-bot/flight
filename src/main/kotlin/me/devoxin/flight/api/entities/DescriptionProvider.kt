package me.devoxin.flight.api.entities

import me.devoxin.flight.internal.entities.ICommand

/**
 * A description provider used for translating keys when flight syncs them.
 *
 * This is more of a sanity thing so that I don't have to go around removing i18n stuff due to a shitty system
 * enforced by Discord.
 */
fun interface DescriptionProvider {
    suspend fun provide(command: ICommand.Slash, description: String): String

    class Default : DescriptionProvider {
        override suspend fun provide(command: ICommand.Slash, description: String) = description
    }
}
