package me.devoxin.flight.internal.entities

import me.devoxin.flight.annotation.FlightPreview
import me.devoxin.flight.api.annotations.RateLimit
import me.devoxin.flight.api.command.Context
import me.devoxin.flight.api.command.message.MessageCommandFunction
import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.message.MessageSubCommandFunction
import me.devoxin.flight.api.command.slash.SlashCommandFunction
import me.devoxin.flight.api.command.slash.SlashSubCommandFunction
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

    interface HasRateLimit : ICommand {
        val rateLimit: RateLimit?
    }

    interface Categorized : ICommand {
        val category: String
    }
}

// TODO: kill me rn

@FlightPreview
val ICommand.botPermissions
    get() = when (this) {
        is SlashSubCommandFunction -> properties.botPermissions
        is SlashCommandFunction -> properties.botPermissions
        is MessageSubCommandFunction -> properties.botPermissions
        is MessageCommandFunction -> properties.userPermissions
        else -> emptyArray()
    }

@FlightPreview
val ICommand.userPermissions
    get() = when (this) {
        is SlashSubCommandFunction -> properties.userPermissions
        is SlashCommandFunction -> properties.userPermissions
        is MessageSubCommandFunction -> properties.userPermissions
        is MessageCommandFunction -> properties.userPermissions
        else -> emptyArray()
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
