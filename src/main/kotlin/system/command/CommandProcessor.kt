package io.iqpizza.system.command

import io.github.oshai.kotlinlogging.KotlinLogging
import io.iqpizza.system.Action
import io.iqpizza.system.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CommandProcessor(
    private val queue: MutableList<Action>,
    private val mutex: Mutex,
    private val systems: List<CommandSystem>,
    private val maxRetries: Int = 3 // 최대 재시도 횟수
) {
    private val logger = KotlinLogging.logger("CommandProcessor")

    suspend fun processCommands(): Action {
        while (true) {
            val action = mutex.withLock {
                if (queue.isNotEmpty()) {
                    val highPriorityIndex = queue.indexOfFirst { it.command.priority == Priority.HIGH }
                    if (highPriorityIndex != -1) {
                        queue.take(highPriorityIndex).first() // HIGH 우선순위 명령 강제 처리
                    } else {
                        queue.firstOrNull()
                    }
                } else {
                    null
                }
            }

            action?.let { act ->
                var executed = false

                systems.forEach { system ->
                    if (system.canExecute(act)) {
                        executed = system.execute(act)
                        if (executed) return@forEach    // 성공 시 반복 종료
                    }
                }

                if (executed) {
                    mutex.withLock { queue.remove(act) }
                    logger.info { "Action ${act.command} executed successfully." }
                } else {
                    handleRetry(act)
                }
            }
            delay(50) // Polling 간격
        }
    }

    private suspend fun handleRetry(action: Action) {
        val command = action.command
        val newRetryCount = command.retryCount + 1

        if (newRetryCount > maxRetries) {
            //TODO: retry 전략: 재시도 지연 시간을 n 제곱만큼 더 대기하도록 한다.
            logger.warn { "Action ${command.type} failed after maximum retries. Discarding. $maxRetries" }
            mutex.withLock { queue.removeAt(0) } // 최대 재시도 초과 시 큐에서 제거
        } else {
            command.retryCount = newRetryCount

            // 큐의 맨 뒤로 이동
            mutex.withLock {
                queue.removeAt(0)
                queue.add(action)
            }
        }
    }
}
