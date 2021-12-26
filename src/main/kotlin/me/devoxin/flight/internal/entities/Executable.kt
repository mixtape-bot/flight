package me.devoxin.flight.internal.entities

import arrow.core.Option
import arrow.core.none
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.arguments.Argument
import me.devoxin.flight.internal.utils.some
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter

public abstract class Executable(
    public val name: String,
    public val method: KFunction<*>,
    public val cog: Cog,
    public val arguments: List<Argument>,
    private val contextParameter: KParameter
) {
    public open suspend fun execute(ctx: Context, args: MutableMap<KParameter, Any?>): Option<Throwable> {
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

            none()
        } catch (ex: Throwable) {
            some(ex.cause ?: ex)
        }
    }
}
