package io.iqpizza.system

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.UnitInPool
import io.iqpizza.unit.GameUnit

/**
 * SC2 와 직접 소통을 수행하는 컨트롤러
 */
class GameController(private val agent: S2Agent) {

    fun fetchUnit(unit: GameUnit): UnitInPool {
        return agent.observation().getUnit(unit.tag)
    }
}
