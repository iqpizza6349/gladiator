package system.command

import io.iqpizza.system.*
import io.iqpizza.system.command.CommandSystem
import io.iqpizza.system.command.CommandProcessor
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CentralCommandManager {
    private val log: Logger = LoggerFactory.getLogger(CentralCommandManager::class.java)

    // 큐 분리
    private val behaviorQueue = mutableListOf<Action>()
    private val buildQueue = mutableListOf<Action>()

    private val behaviorMutex = Mutex()
    private val buildMutex = Mutex()

    private val commandSystems = mutableListOf<CommandSystem>()
    private val scope = CoroutineScope(Dispatchers.Default)

    private var started: Boolean = false

    fun registerCommandSystem(system: CommandSystem) {
        commandSystems.add(system)
        log.info("Registered CommandSystem: {}", system::class.simpleName)
    }

    // Command 추가
    fun addCommand(command: Command, behavior: Behavior) {
        val action = createAction(command, behavior)
        when (command.type) {
            CommandType.MOVE, CommandType.ATTACK                                           -> {
                synchronized(behaviorQueue) {
                    behaviorQueue.add(action)
                    behaviorQueue.sortBy { it.command.priority }
                }
            }

            CommandType.RESEARCH, CommandType.BUILD, CommandType.TRAIN, CommandType.SUPPLY -> {
                synchronized(buildQueue) {
                    buildQueue.add(action)
                    behaviorQueue.sortBy { it.command.priority }
                }
            }
        }
        log.info("Added Command: {}", command)
    }

    private fun createAction(command: Command, execute: Behavior): Action {
        return Action(
            command = command,
            execute = execute
        )
    }

    // Command 취소
//    fun cancelCommand(command: Command) {
//        when (command.type) {
//            CommandType.MOVE, CommandType.ATTACK                                           -> behaviorQueue.remove(
//                command
//            )
//
//            CommandType.RESEARCH, CommandType.BUILD, CommandType.TRAIN, CommandType.SUPPLY -> buildQueue.remove(command)
//        }
//        log.info("Cancelled Command: {}", command)
//    }

    fun startProcessing() {
        if (started) {
            throw IllegalStateException("Already started")
        }

        started = true

        val behaviorProcessor = CommandProcessor(behaviorQueue, behaviorMutex, commandSystems.toList())
        val buildProcessor = CommandProcessor(buildQueue, buildMutex, commandSystems.toList())

        scope.launch { behaviorProcessor.processCommands() }
        scope.launch { buildProcessor.processCommands() }
    }

    fun stopProcessing() {
        scope.cancel()
        started = false
        log.info("Stopped processing queues.")
    }
}
