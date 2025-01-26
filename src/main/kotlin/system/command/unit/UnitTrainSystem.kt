package io.iqpizza.system.command.unit

import com.github.ocraft.s2client.protocol.data.Abilities
import io.iqpizza.system.CommandType
import io.iqpizza.system.GameController
import io.iqpizza.system.Action
import io.iqpizza.system.command.CommandSystem
import io.iqpizza.unit.Filters
import io.iqpizza.unit.UnitMetadata
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * 유닛을 생성하는 시스템이며, 명령 기반입니다.
 * 이떄, 유닛 생산의 포함 범주는 이동 가능한 유닛이며, 변태(예. 저글링에서 맹독충으로) 역시 포함합니다.
 * <pre>
 *     data 는 <GameUnit, Units> 형태입니다.
 * </pre>
 */
class UnitTrainSystem(override val gameController: GameController) : CommandSystem {
    private val abilityPrefix = "TRAIN_"

    override fun canExecute(action: Action): Boolean {
        val command = action.command
        val ability = command.data.second
        if (command.type !== CommandType.TRAIN) {
            return false
        } else if (!isTrainAbility(ability)) {
            return false
        }
        return canTrain(ability)
    }

    override fun execute(action: Action): Boolean {
        return if (canExecute(action)) {
            runBlocking {
                val result = async {
                    action.execute(gameController, action.command.data)
                }

                return@runBlocking result.await()
            }
        } else {
            false
        }
    }

    private fun canTrain(trainAbility: Abilities): Boolean {
        // train 가능한 유닛 리스트들을 찾는다.
        val trainAvailableUnits = Filters.getAvailableUnits(trainAbility)

        // 해당 유닛을 내가 소지하고 있는 지 체크해야한다.
        val exists = gameController.existsUnitType(trainAvailableUnits)
        if (!exists) {
            return false
        }

        // 현재 해당 유닛을 생산할 수 있을 정도의 인구수를 가지고 있는 지 체크
        val unitMetadata = UnitMetadata.findMetaData(trainAbility)
        val remainingSupply = gameController.fetchRemainingSupply()
        val maxSupply = gameController.fetchMaxSupply()
        if (remainingSupply + unitMetadata.supplyCost > maxSupply) {
            // 현재 남은 인구수 + 생산할 유닛의 인구수 <= 최대 인구수 인 경우에만 생산 허용
            return false
        }

        // 자원이 충분한가?
        val enoughResource = gameController.enoughResource(
            requiredMinerals = unitMetadata.mineralCost,
            requiredVespene = unitMetadata.gasCost
        )
        return enoughResource
    }

    private fun isTrainAbility(ability: Abilities): Boolean {
        return ability.name.startsWith(abilityPrefix)
    }
}