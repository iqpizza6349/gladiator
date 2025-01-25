package io.iqpizza.system.command

import io.iqpizza.system.Command
import io.iqpizza.system.GameController

interface CommandSystem {
    val gameController: GameController

    fun canExecute(command: Command): Boolean
    fun execute(command: Command): Boolean
}
