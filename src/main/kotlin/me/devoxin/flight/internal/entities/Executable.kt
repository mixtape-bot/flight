package me.devoxin.flight.internal.entities

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
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

    open fun execute(ctx: Context, args: HashMap<KParameter, Any?>, complete: (Boolean, Throwable?) -> Unit, dispatcher: CoroutineDispatcher) {
        method.instanceParameter?.let { args[it] = cog }
        args[contextParameter] = ctx

        ctx.commandClient.launch(ctx.commandClient.coroutineContext) {
            if (method.isSuspend) {
                executeAsync(args, complete)
            } else {
                executeSync(args, complete)
            }
        }
    }

    /**
     * Calls the related method with the given args.
     */
    private fun executeSync(args: HashMap<KParameter, Any?>, complete: (Boolean, Throwable?) -> Unit) {
        try {
            method.callBy(args)
            complete(true, null)
        } catch (e: Throwable) {
            complete(false, e.cause ?: e)
        }
    }

    /**
     * Calls the related method with the given args, except in an async manner.
     */
    private suspend fun executeAsync(args: HashMap<KParameter, Any?>, complete: (Boolean, Throwable?) -> Unit) {
        try {
            method.callSuspendBy(args)
            complete(true, null)
        } catch (e: Throwable) {
            complete(false, e.cause ?: e)
        }
    }

}
