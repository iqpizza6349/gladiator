package game

import kotlin.math.round

data class InGameClock(private val gameLoopsPerSecond: Double) {

    // game-loop 를 기반으로 현재 시간을 초 단위로 계산 (반올림 하도록 한다.)
    fun calculateSeconds(currentGameLoop: Long): Double {
        return round(currentGameLoop / gameLoopsPerSecond)
    }
}
