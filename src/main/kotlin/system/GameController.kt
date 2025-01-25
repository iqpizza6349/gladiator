package io.iqpizza.system

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Units
import com.github.ocraft.s2client.protocol.unit.Alliance
import com.github.ocraft.s2client.protocol.unit.Unit
import io.iqpizza.unit.Filters
import io.iqpizza.unit.GameUnit

/**
 * SC2 와 직접 소통을 수행하는 컨트롤러
 */
class GameController(private val agent: S2Agent) {

    fun fetchUnit(unit: GameUnit): UnitInPool {
        return agent.observation().getUnit(unit.tag)
    }

    fun existsUnitType(units: Collection<Units> = emptySet()): Boolean {
        return fetchUnit(units).isNotEmpty()
    }

    // 남은 인구수 조회
    fun fetchRemainingSupply(): Int {
        val maxSupply = fetchMaxSupply()    // 인구수 최대 공급량
        val usedSupply = fetchUsedSupply()   // 사용한 인구수
        return maxSupply - usedSupply
    }

    fun fetchMaxSupply(): Int {
        return agent.observation().foodCap
    }

    fun fetchUsedSupply(): Int {
        return agent.observation().foodUsed
    }

    fun fetchArmySupply(): Int {
        return agent.observation().foodArmy
    }

    fun fetchWorkerSupply(): Int {
        return agent.observation().foodWorkers
    }

    fun enoughResource(requiredMinerals: Int, requiredVespene: Int): Boolean {
        val currentMineral = agent.observation().minerals
        val currentVespene = agent.observation().vespene
        return currentMineral >= requiredMinerals && currentVespene >= requiredVespene
    }

    fun trainUnit(issuers: Collection<Units>, desireTo: Abilities) : Boolean {
        val idleUnit = fetchIdleUnit(issuers) ?: return false
        agent.actions().unitCommand(idleUnit, desireTo, true)
        return true
    }

    fun fetchIdleUnit(unitTypes: Collection<Units>): Unit? {
        val filter = fetchUnit(unitTypes).filter { Filters.isIdle(it) }
        val unitInPool = filter.firstOrNull() ?: return null
        val idleUnit = unitInPool.unit ?: return null
        return  idleUnit.get()
    }

    fun fetchUnit(unitTypes: Collection<Units>, alliance: Alliance = Alliance.SELF): List<UnitInPool> {
        return try {
            agent.observation().getUnits { Filters.isUnitType(it, unitTypes, alliance) }
        } catch (e: ConcurrentModificationException) {
            // Java 와 Kotlin 차이로 인해 발생함
            emptyList()
        }
    }
}
