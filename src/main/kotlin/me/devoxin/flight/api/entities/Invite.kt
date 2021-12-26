package me.devoxin.flight.api.entities

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.requests.RestAction

public data class Invite(val jda: JDA, val url: String, val code: String) {
    public suspend fun resolve(): Invite? =
        Invite.resolve(jda, code).submit().await()
}
