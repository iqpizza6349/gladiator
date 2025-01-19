package io.iqpizza

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.Level
import io.iqpizza.utils.StopWatch

abstract class GameInitializer(protected val stopWatch: StopWatch) {
    companion object {
        private val supportsLevel = arrayOf(
            Level.INFO,
            Level.WARN,
            Level.TRACE,
        )
    }

    abstract fun logger(): KLogger

    fun logDetailResult(level: Level = Level.INFO) {
        when (level) {
            Level.TRACE -> logger().trace { stopWatch.prettyPrint() }
            Level.DEBUG -> logger().debug { stopWatch.prettyPrint() }
            Level.INFO  -> logger().info { stopWatch.prettyPrint() }
            else        -> return
        }
    }

    fun logResult(level: Level = Level.INFO) {
        if (level !in supportsLevel) {
            throw IllegalArgumentException("Level $level is not supported")
        }

        val message = "'${stopWatch.id}' takes ${stopWatch.totalTimeMillis} ms. (Total Tasks: ${stopWatch.taskCount})"

        when (level) {
            Level.TRACE -> logger().trace { message }
            Level.DEBUG -> logger().debug { message }
            Level.INFO  -> logger().info { message }
            else        -> return
        }
    }

}