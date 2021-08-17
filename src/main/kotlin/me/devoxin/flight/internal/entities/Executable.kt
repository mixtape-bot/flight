package me.devoxin.flight.internal.entities

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.Argument
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter

abstract class Executable(
    val name: String,
    val method: KFunction<*>,
    val cog: Cog,
    val arguments: List<Argument>,
    private val contextParameter: KParameter
) {
    open suspend fun execute(ctx: Context, args: HashMap<KParameter, Any?>): Result<Boolean> {
        method.instanceParameter?.let { args[it] = cog }

        /* put the context parameter in place. */
        args[contextParameter] = ctx

        /* execute the command. */
        return try {
            if (method.isSuspend) {
                method.callSuspendBy(args)
            } else {
                method.callBy(args)
            }

            Result.success(true)
        } catch (ex: Throwable) {
            Result.failure(ex)
        }
    }
}
