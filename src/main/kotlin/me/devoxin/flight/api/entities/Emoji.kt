package me.devoxin.flight.api.entities

public data class Emoji(val name: String, val id: Long, val animated: Boolean) {
    val url: String
        get() = "https://cdn.discordapp.com/emojis/$id.${"gif".takeIf { animated } ?: "png"}"
}
