package me.devoxin.flight.internal.utils

import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.command.message.annotations.*
import me.devoxin.flight.api.entities.Cog
import org.reflections.Reflections
import org.reflections.scanners.MethodParameterNamesScanner
import org.reflections.scanners.SubTypesScanner
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

@OptIn(FlightPreview::class)
class Indexer(private val packageName: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(Indexer::class.java)
    }

    private val reflections: Reflections = Reflections(packageName, MethodParameterNamesScanner(), SubTypesScanner())

    fun getCogs(): List<Cog> {
        val cogs = reflections.getSubTypesOf(Cog::class.java)
        logger.debug("Discovered ${cogs.size} cogs in $packageName")

        return cogs
            .filter { !Modifier.isAbstract(it.modifiers) && !it.isInterface && Cog::class.java.isAssignableFrom(it) }
            .map { it.getDeclaredConstructor().newInstance() }
    }

    fun getMessageCommands(cog: Cog): List<KFunction<*>> {
        logger.debug("Scanning ${cog::class.simpleName} for message commands...")

        val cogClass = cog::class
        val commands = cogClass.members
            .filterIsInstance<KFunction<*>>()
            .filter { it.hasAnnotation<MessageCommand>() }

        logger.debug("Found ${commands.size} commands in cog ${cog::class.simpleName}")
        return commands.toList()
    }
}
