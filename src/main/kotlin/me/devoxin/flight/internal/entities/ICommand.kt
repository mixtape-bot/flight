package me.devoxin.flight.internal.entities

import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.CommandArgument
import javax.naming.OperationNotSupportedException
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter

/**
 * A command executor.
 */
interface ICommand {
    val name: String
    val method: KFunction<*>?
    val cog: Cog
    val arguments: List<CommandArgument>
    val contextParameter: KParameter?
    val declaringClass: Any? get() = cog

    interface Slash : ICommand
    interface Message : ICommand
    interface Categorized : ICommand {
        val category: String
    }
}

suspend fun ICommand.execute(ctx: Context, args: MutableMap<KParameter, Any?>): Result<Boolean> {
    method ?: throw OperationNotSupportedException()
    method!!.instanceParameter?.let { args[it] = declaringClass }

    /* put the context parameter in place. */
    args[contextParameter ?: throw error("Missing contextParameter field.")] = ctx

    /* execute the command. */
    return try {
        if (method!!.isSuspend) {
            method!!.callSuspendBy(args)
        } else {
            method!!.callBy(args)
        }

        Result.success(true)
    } catch (ex: Throwable) {
        Result.failure(ex)
    }
}
