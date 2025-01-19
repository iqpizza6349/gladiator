package io.iqpizza.game

import com.github.ocraft.s2client.protocol.game.PlayerInfo
import com.github.ocraft.s2client.protocol.game.Race
import com.github.ocraft.s2client.protocol.game.raw.StartRaw
import io.iqpizza.map.Position
import io.iqpizza.map.Region
import io.iqpizza.map.Tile
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



}