package me.devoxin.flight.api

import me.devoxin.flight.internal.entities.CommandRegistry

internal object Commands {
    fun findCommand(registry: CommandRegistry, content: String): FoundCommand? {
        val command = registry.values.firstOrNull { it.triggeredBy(content) }
            ?: return null

        return FoundCommand(command.findTrigger(content)!!, command)
    }

    data class FoundCommand(val trigger: String, val command: CommandFunction)

    fun findSubCommand(command: CommandFunction, content: String): FoundSubCommand? {
        val subCommand = command.subcommands.values.firstOrNull { it.triggeredBy(content) }
            ?: return null

        return FoundSubCommand(subCommand.findTrigger(content)!!, subCommand)
    }

    data class FoundSubCommand(val trigger: String, val command: SubCommandFunction)
}
