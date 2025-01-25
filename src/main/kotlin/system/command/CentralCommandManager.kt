package system.command

import io.iqpizza.system.Command
import io.iqpizza.system.command.CommandSystem
import io.iqpizza.system.CommandType
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.PriorityQueue

class CentralCommandManager {
    private val log: Logger = LoggerFactory.getLogger(CentralCommandManager::class.java)

    // 큐 분리
    private val movementQueue = PriorityQueue<Command> { c1, c2 -> c1.priority.compareTo(c2.priority) }
    private val buildQueue = PriorityQueue<Command> { c1, c2 -> c1.priority.compareTo(c2.priority) }

    private val commandSystems = mutableListOf<CommandSystem>()
    private val scope = CoroutineScope(Dispatchers.IO)

    private var started: Boolean = false

    fun registerCommandSystem(system: CommandSystem) {
        commandSystems.add(system)
        log.info("Registered CommandSystem: {}", system::class.simpleName)
    }

    // Command 추가
    fun addCommand(command: Command) {
        when (command.type) {
            CommandType.MOVE, CommandType.ATTACK                                           -> movementQueue.add(command)
            CommandType.RESEARCH, CommandType.BUILD, CommandType.TRAIN, CommandType.SUPPLY -> buildQueue.add(command)
        }
        log.info("Added Command: {}", command)
    }

    // Command 취소
    fun cancelCommand(command: Command) {
        when (command.type) {
            CommandType.MOVE, CommandType.ATTACK                                           -> movementQueue.remove(
                command
            )

            CommandType.RESEARCH, CommandType.BUILD, CommandType.TRAIN, CommandType.SUPPLY -> buildQueue.remove(command)
        }
        log.info("Cancelled Command: {}", command)
    }

    fun startProcessing() {
        if (started) {
            throw IllegalStateException("Already started")
        }

        started = true

        scope.launch { processQueue(movementQueue, "Movement Queue") }
        scope.launch { processQueue(buildQueue, "Build Queue") }
    }

    private suspend fun processQueue(queue: PriorityQueue<Command>, queueName: String) {
        while (true) {
            if (queue.isNotEmpty()) {
                val command = queue.peek()
                val system = commandSystems.find { it.canExecute(command) }

                if (system != null) {
                    if (system.execute(command)) {
                        log.info("Executed $queueName Command: {}", command)
                        queue.peek()
                    } else {
                        log.warn("Failed to execute $queueName Command: {}", command)
                    }
                } else {
                    log.warn("No system available to execute $queueName Command: {}", command)
                }
            }
            delay(100) // Queue 체크 주기
        }
    }

    fun stopProcessing() {
        scope.cancel()
        started = false
        log.info("Stopped processing queues.")
    }
}
