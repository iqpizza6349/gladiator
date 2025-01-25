package io.iqpizza.system.command.unit

import com.github.ocraft.s2client.protocol.data.Abilities
import com.github.ocraft.s2client.protocol.data.Units
import io.iqpizza.game.Game
import io.iqpizza.system.Command
import io.iqpizza.system.CommandType
import io.iqpizza.system.GameController
import io.iqpizza.system.command.CommandSystem
import io.iqpizza.unit.UnitMetadata

/**
 * 유닛을 생성하는 시스템이며, 명령 기반입니다.
 * 이떄, 유닛 생산의 포함 범주는 이동 가능한 유닛이며, 변태(예. 저글링에서 맹독충으로) 역시 포함합니다.
 * <pre>
 *     data 는 <GameUnit, Units> 형태입니다.
 * </pre>
 */
class UnitTrainSystem(override val gameController: GameController) : CommandSystem {
    private val abilityPrefix = "TRAIN_"

    override fun canExecute(command: Command): Boolean {
        return (command.type == CommandType.TRAIN)
    }

    override fun execute(command: Command): Boolean {
        val data = command.data
        val desireUnitType = data.second as Units

        val (canTrain, availableUnits) = canTrain(desireUnitType)

        if (!canTrain) {
            // 생산 조건에 부합하지 못했을 때
            return false
        }

        val ability = getAbility(desireUnitType)
        return gameController.trainUnit(availableUnits, ability)
    }

    private fun canTrain(desireUnitType: Units): Pair<Boolean, Set<Units>> {
        // train 가능한 유닛 리스트들을 찾는다.
        val trainAvailableUnits = getTrainAvailableUnits(desireUnitType)

        // 해당 유닛을 내가 소지하고 있는 지 체크해야한다.
        val exists = gameController.existsUnitType(trainAvailableUnits)
        if (!exists) {
            return Pair(false, emptySet())
        }

        // 현재 해당 유닛을 생산할 수 있을 정도의 인구수를 가지고 있는 지 체크
        val unitMetadata = UnitMetadata.findMetaData(desireUnitType)
        val remainingSupply = gameController.fetchRemainingSupply()
        val maxSupply = gameController.fetchMaxSupply()
        if (remainingSupply + unitMetadata.supplyCost > maxSupply) {
            // 현재 남은 인구수 + 생산할 유닛의 인구수 <= 최대 인구수 인 경우에만 생산 허용
            return Pair(false, emptySet())
        }

        // 자원이 충분한가?
        val enoughResource = gameController.enoughResource(
            requiredMinerals = unitMetadata.mineralCost,
            requiredVespene = unitMetadata.gasCost
        )
        return Pair(enoughResource, trainAvailableUnits)
    }

    private fun getTrainAvailableUnits(desireUnit: Units): Set<Units> {
        val trainAbility = getAbility(desireUnit)
        val trainAvailableUnits = mutableSetOf<Units>()

        val allUnitTypes = Units.entries.toTypedArray()
        for (unitType in allUnitTypes) {
            if (trainAbility in unitType.abilities) {
                trainAvailableUnits.add(unitType)
            }
        }

        return trainAvailableUnits
    }

    private fun getAbility(desireUnit: Units): Abilities {
        val race = Game.playerRace.name + "_"   // ex. TERRAN_, ZERG_, PROTOSS_
        val desireType = desireUnit.name.replace(race, "")   // remove to find starts with "TRAIN_"
        return Abilities.valueOf(abilityPrefix + desireType)   // ex. TRAIN_SCV, TRAIN_BANELING
    }
}