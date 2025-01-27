package io.iqpizza.game

import com.github.ocraft.s2client.protocol.game.PlayerInfo
import com.github.ocraft.s2client.protocol.game.Race
import com.github.ocraft.s2client.protocol.game.raw.StartRaw
import com.github.ocraft.s2client.protocol.unit.Alliance
import game.InGameClock
import io.iqpizza.map.Position
import io.iqpizza.map.Region
import io.iqpizza.map.Tile
import io.iqpizza.system.GameController
import io.iqpizza.unit.GameUnit
import io.iqpizza.unit.Owner
import map.RegionArea
import kotlin.properties.Delegates

object Game {

    /**
     * 진행 중인 게임의 맵 이름입니다.
     */
    lateinit var mapName: String

    var mapHeight by Delegates.notNull<Int>()
    var mapWidth by Delegates.notNull<Int>()

    lateinit var allTiles: List<Tile>
    lateinit var expansionAreas: List<RegionArea>
    lateinit var allRegions: List<Region>
    lateinit var chokePoints: List<Position>        //TODO: 추후 chokePoint 데이터 클래스를 사용하면 더 좋을 까?

    /**
     * 기본적인 게임 시작과 동시에 raw 하게 얻는 데이터
     */
    lateinit var startRaw: StartRaw

    /**
     * Player, Computer, Observer 모두 포함
     */
    lateinit var players: Set<PlayerInfo>

    /**
     * player id
     */
    var selfId by Delegates.notNull<Int>()

    /**
     * 플레이어(클라이언트)의 종족
     */
    lateinit var playerRace: Race

    /**
     * 상대(적)의 종족.
     * 초기에는 상세히 알 수 없기 떄문에 RANDOM 으로 할당 후
     * 정해지면 갱신하도록 한다.
     */
    var enemyRace: Race = Race.RANDOM

    /**
     * 인-게임 속도를 설정합니다. 게임 속도는 [gameClock] 의 영향을 미칩니다.
     *
     * 기본 게임 속도는 NORMAL 로 설정합니다.
     */
    var gameSpeed: GameSpeed by Delegates.observable(GameSpeed.NORMAL) { _, _, newSpeed ->
        gameClock = InGameClock(newSpeed.perFrameCount)
    }

    /**
     * gameClock 은 인-게임 속도의 영향을 받아 시간을 계산합니다.
     * 주로 [io.iqpizza.system.TimedSystem] 에 사용되거나 현재 시간을 정확히 측정할 때 사용합니다.
     */
    var gameClock = InGameClock(gameSpeed.perFrameCount)
        private set

    /**
     * Agent 와 직접 소통하며, 각 시스템에서 호춯하는 컨트롤러입니다.
     */
    var gameController: GameController by Delegates.notNull()

    private val positions = mutableMapOf<GameUnit, Position>()
    private val owners = mutableMapOf<GameUnit, Owner>()

    fun addPosition(unit: GameUnit, position: Position) {
        positions[unit] = position
    }

    fun getPosition(unit: GameUnit): Position? = positions[unit]

    fun removePosition(unit: GameUnit) {
        positions.remove(unit)
    }

    fun addOwner(unit: GameUnit, owner: Owner = Owner.SELF) {
        owners[unit] = owner
    }

    private fun removeOwner(unit: GameUnit) {
        // Owner 를 삭제해야할 때에는 유닛이 죽었을 때만 적용한다.
        owners.remove(unit)
    }

    fun removeAll(unit: GameUnit) {
        removeOwner(unit)
        removePosition(unit)
    }

    fun getPlayerUnits(alliance: Alliance = Alliance.SELF): Set<GameUnit> {
        return getAllUnits().filter { unit ->
            owners[unit]?.alliance == alliance
        }.toSet()
    }

    fun getAllUnits(): Set<GameUnit> {
        return positions.keys.toSet()
    }

    fun getPositions() = positions.entries
}