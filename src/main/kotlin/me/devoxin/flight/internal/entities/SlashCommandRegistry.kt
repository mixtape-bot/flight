package me.devoxin.flight.internal.entities

import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.command.slash.SlashCommandFunction
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.utils.Indexer
import me.devoxin.flight.internal.utils.SlashCommandUtil

@OptIn(FlightPreview::class)
class SlashCommandRegistry : HashMap<String, SlashCommandFunction>() {
    fun findCommandByName(name: String): SlashCommandFunction? {
        return this[name]
    }

    fun findCogByName(name: String): Cog? {
        return this.values.firstOrNull { it.cog::class.simpleName == name }?.cog
    }

    fun findCommandsByCog(cog: Cog): List<SlashCommandFunction> {
        return this.values.filter { it.cog == cog }
    }

    fun unload(commandFunction: SlashCommandFunction) {
        this.values.remove(commandFunction)
    }

    fun unload(cog: Cog) {
        val commands = this.values.filter { it.cog == cog }
        this.values.removeAll(commands)
    }

    fun register(packageName: String) {
        val indexer = Indexer(packageName)
        for (cog in indexer.getCogs()) {
            register(cog)
        }
    }

    fun register(cog: Cog) {
        val commands = SlashCommandUtil.loadCog(cog)
        for (command in commands) {
            if (this.containsKey(command.name)) {
                throw RuntimeException("Cannot register command ${command.name}; the trigger has already been registered.")
            }

            this[command.name] = command
        }
    }
}
