package me.devoxin.flight.internal.utils

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.SubCommandFunction
import me.devoxin.flight.api.annotations.*
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.Argument
import org.reflections.Reflections
import org.reflections.scanners.MethodParameterNamesScanner
import org.reflections.scanners.SubTypesScanner
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

class Indexer(private val packageName: String) {

  private val reflections: Reflections = Reflections(packageName, MethodParameterNamesScanner(), SubTypesScanner())

  fun getCogs(): List<Cog> {
    val cogs = reflections.getSubTypesOf(Cog::class.java)
    log.debug("Discovered ${cogs.size} cogs in $packageName")

    return cogs
      .filter { !Modifier.isAbstract(it.modifiers) && !it.isInterface && Cog::class.java.isAssignableFrom(it) }
      .map { it.getDeclaredConstructor().newInstance() }
  }

  fun getCommands(cog: Cog): List<KFunction<*>> {
    log.debug("Scanning ${cog::class.simpleName} for commands...")

    val cogClass = cog::class
    val commands = cogClass.members
      .filterIsInstance<KFunction<*>>()
      .filter { it.hasAnnotation<Command>() }

    log.debug("Found ${commands.size} commands in cog ${cog::class.simpleName}")
    return commands.toList()
  }

  fun loadCommand(meth: KFunction<*>, cog: Cog): CommandFunction {
    val name = meth.findAnnotation<Name>()?.name
      ?: meth.name

    check(meth.javaMethod!!.declaringClass == cog::class.java) {
      "$name is not from ${cog::class.simpleName}"
    }

    require(meth.hasAnnotation<Command>()) {
      "$name is not annotated with Command!"
    }

    val category = cog.name()
      ?: cog::class.java.`package`.name.split('.')
        .last()
        .replace('_', ' ')
        .lowercase()
        .replaceFirstChar { it.uppercase() }

    val properties = meth.findAnnotation<Command>()!!
    val cooldown = meth.findAnnotation<Cooldown>()
    val ctxParam = meth.valueParameters.firstOrNull {
      it.type.classifier?.equals(Context::class) == true
    }

    require(ctxParam != null) {
      "${meth.name} is missing the Context parameter!"
    }

    val parameters = meth.valueParameters.filterNot {
      it.type.classifier?.equals(Context::class) == true
    }

    val arguments = loadParameters(parameters)
    val subcommands = getSubCommands(cog)
    val cogParentCommands = cog::class.functions.filter {
      it.hasAnnotation<Command>()
    }

    if (subcommands.isNotEmpty() && cogParentCommands.size > 1) {
      throw IllegalStateException("SubCommands are present within ${cog::class.simpleName} however there are multiple top-level commands!")
    }

    return CommandFunction(name, category, properties, cooldown, subcommands, meth, cog, arguments, ctxParam)
  }

  fun getSubCommands(cog: Cog): List<SubCommandFunction> {
    log.debug("Scanning ${cog::class.simpleName} for sub-commands...")

    val cogClass = cog::class
    val subcommands = cogClass.members
      .filterIsInstance<KFunction<*>>()
      .filter { it.hasAnnotation<SubCommand>() }
      .map { loadSubCommand(it, cog) }

    log.debug("Found ${subcommands.size} sub-commands in cog ${cogClass.simpleName}")
    return subcommands.toList()
  }

  private fun loadSubCommand(meth: KFunction<*>, cog: Cog): SubCommandFunction {
    /* get the name of this sub-command */
    val name = meth.findAnnotation<Name>()?.name
      ?: meth.name.lowercase()

    /* do some checks */
    check(meth.javaMethod!!.declaringClass == cog::class.java) {
      "${meth.name} is not from ${cog::class.simpleName}"
    }

    require(meth.hasAnnotation<SubCommand>()) {
      "${meth.name} is not annotated with SubCommand!"
    }

    /* load properties */
    val properties = meth.findAnnotation<SubCommand>()!!

    /* find the context parameter. */
    val ctxParam = meth.valueParameters.firstOrNull {
      it.type.classifier?.equals(Context::class) == true
    }

    require(ctxParam != null) {
      "${meth.name} is missing the Context parameter!"
    }

    /* find arguments */
    val parameters = meth.valueParameters.filterNot {
      it.type.classifier?.equals(Context::class) == true
    }

    val arguments = loadParameters(parameters)

    /* return sub command function */
    return SubCommandFunction(name, properties, meth, cog, arguments, ctxParam)
  }

  private fun loadParameters(parameters: List<KParameter>): List<Argument> {
    val arguments = mutableListOf<Argument>()

    for (p in parameters) {
      val pName = p.findAnnotation<Name>()?.name ?: p.name ?: p.index.toString()
      val type = p.type.jvmErasure.javaObjectType
      val isGreedy = p.hasAnnotation<Greedy>()
      val isOptional = p.isOptional
      val isNullable = p.type.isMarkedNullable
      val isTentative = p.hasAnnotation<Tentative>()

      if (isTentative && !(isNullable || isOptional)) {
        throw IllegalStateException("${p.name} is marked as tentative, but does not have a default value and is not marked nullable!")
      }

      arguments.add(Argument(pName, type, isGreedy, isOptional, isNullable, isTentative, p))
    }

    return arguments
  }

  companion object {
    private val log = LoggerFactory.getLogger(Indexer::class.java)
  }

}
