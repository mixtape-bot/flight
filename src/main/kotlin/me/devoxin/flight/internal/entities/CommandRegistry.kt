package me.devoxin.flight.internal.entities

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.utils.Indexer

public class CommandRegistry : HashMap<String, CommandFunction>() {
    public fun findCommandByName(name: String): CommandFunction? {
        return this.values.firstOrNull { it.name.equals(name, true) }
    }

    public fun findCommandByAlias(alias: String): CommandFunction? {
        return this.values.firstOrNull { it.properties.aliases.any { a -> a.equals(alias, true) } }
    }

    public fun findCogByName(name: String): Cog? {
        return this.values.firstOrNull { it.cog::class.simpleName == name }?.cog
    }

    public fun findCommandsByCog(cog: Cog): List<CommandFunction> {
        return this.values.filter { it.cog == cog }
    }

    public fun unload(commandFunction: CommandFunction) {
        this.values.remove(commandFunction)
    }

    public fun unload(cog: Cog) {
        val commands = this.values.filter { it.cog == cog }
        this.values.removeAll(commands)
    }

    public fun register(packageName: String) {
        val indexer = Indexer(packageName)
        for (cog in indexer.getCogs()) {
            register(cog, indexer)
        }
    }

    public fun register(cog: Cog, indexer: Indexer? = null) {
        val i = indexer ?: Indexer(cog::class.java.getPackage().name)
        val commands = i.getCommands(cog)

        for (command in commands) {
            val cmd = i.loadCommand(command, cog)

            if (this.containsKey(cmd.name)) {
                throw RuntimeException("Cannot register command ${cmd.name}; the trigger has already been registered.")
            }

            this[cmd.name] = cmd
        }
    }
}
