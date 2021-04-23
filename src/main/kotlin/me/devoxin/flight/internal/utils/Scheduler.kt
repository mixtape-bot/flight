package me.devoxin.flight.internal.utils

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object Scheduler {
  private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor {
    Thread(it, "Flight Executor")
  }

  /**
   * Calls [block] every [delay].
   *
   * @param delay
   *   How long to wait before each execution.
   *
   * @param timeUnit
   *   Time unit used for [delay]
   *
   * @param block
   *   The block to call.
   */
  fun every(delay: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS, block: () -> Unit): ScheduledFuture<*> {
    return executor.scheduleAtFixedRate(block, delay, delay, timeUnit)
  }
}