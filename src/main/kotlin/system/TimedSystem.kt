package io.iqpizza.system

import game.InGameClock
import io.iqpizza.game.Game

class TimedSystem (
    private val gameClock: InGameClock,
    private val actionIntervalSeconds: Double = 1.0,
    private val action: ((gameController: GameController) -> Unit)
) {
    private var lastExecutionTime = 0.0

    fun update(currentGameLoop: Long) {
        val currentTime = gameClock.calculateSeconds(currentGameLoop)

        // 동작 주기가 되었는 지 체크
        if (currentTime - lastExecutionTime >= actionIntervalSeconds) {
            action(Game.gameController)
            lastExecutionTime = currentTime
        }
    }
}
