package io.iqpizza.system.timed.unit

import io.github.oshai.kotlinlogging.KotlinLogging
import io.iqpizza.game.Game
import io.iqpizza.map.Position
import io.iqpizza.system.GameController
import io.iqpizza.unit.Filters
import io.iqpizza.utils.toGameUnit

object UnitSystem {
    private val logger = KotlinLogging.logger("UnitSystem")

    /**
     * 플레이어의 유닛(건물 제외)들의 위치를 갱신한다.
     */
    fun updatePosition(gameController: GameController) {
        // 플레이어 유닛만 조회한다.
        val playerUnits = Game.getPlayerUnits()
        val pools = playerUnits.map(gameController::fetchUnit)
        val excludeBuildings = pools.filter { !Filters.isBuilding(it) }

        for (unit in excludeBuildings) {
            if (!unit.isAlive) {
                // 죽은 유닛의 경우
                Game.removeAll(unit.tag.toGameUnit())
                break
            }

            val rawUnit = unit.unit
            rawUnit.ifPresentOrElse({
                // 실제 데이터가 존재하는 경우에 한해서만 업데이트를 수행한다.
                Game.addPosition(it.toGameUnit(), Position.fromPoint3d(it.position))
            }, {
                // 실제 데이터가 누락된 경우, 죽은 것과 동일 취급한다.
                Game.removeAll(unit.tag.toGameUnit())
            })
        }
    }
}
