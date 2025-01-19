package io.iqpizza

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.game.PlayerInfo
import com.github.ocraft.s2client.protocol.game.PlayerType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.iqpizza.game.Game
import io.iqpizza.game.PlayerInitializer
import io.iqpizza.map.MapInitializer
import io.iqpizza.map.Position
import io.iqpizza.system.GameController
import io.iqpizza.utils.StopWatch
import io.iqpizza.utils.toGameUnit
import map.SimpleArea
import utils.generateRegions

class Gladiator : S2Agent() {
    companion object {
        private val log = KotlinLogging.logger("Gladiator")
    }

    init {
        Game.gameController = GameController(this)
    }

    /**
     * 게임이 최초 로드가 완료된 이후에 호출되는 메소드입니다.
     * 즉, [onGameFullStart] 가 완료된 이후에 호출됩니다.
     *
     * startLocation 갱신은 해당 메소드가 호출되기 직전에 호출된다.
     */
    override fun onGameStart() {
        val stopWatch = StopWatch("onGameStart")
        val mapInitializer = MapInitializer(stopWatch)

        // 모든 Base(자원이 주변에 존재하는 영역) 들을 포함한 컬렉션
        val bases = mutableSetOf<SimpleArea>()

        // 1. 플레이어 본인의 진영을 등록한다.
        mapInitializer.initializePlayerRegion(observation().startLocation, bases)

        // 2. 적 본진이 될 수 있는 진영을 등록한다.
        mapInitializer.initializeEnemiesRegion(bases)

        // 3. 나머지 모든 진영들 중 적 본진 대상 가능성이 존재하는 영토를 제외하여, 모두 등록한다.
        val expansionLocations = query().calculateExpansionLocations(observation())
        mapInitializer.initializeAllRegions(bases) { expansionLocations }

        // 4. 모든 진영들에 대해 검증 및 타일 인접 추가
        stopWatch.start("Validate All Regions")
        val walkableTiles = Game.allTiles.filter { tile -> tile.walkable }
        Game.allRegions = generateRegions(walkableTiles, bases)
        stopWatch.stop()

        // 5. Area 초기화
        mapInitializer.initializeAllArea(bases)

        // 6. chokePoint(길목) 초기화
        val allTiles = Game.allTiles.associateBy { it.position }
        mapInitializer.initializeChokePoints(allTiles)

        mapInitializer.logDetailResult()
        mapInitializer.logResult()
    }

    /**
     * 게임이 최초 로드되었을 때 호출되는 메소드입니다.
     * startLocation 갱신은 해당 메소드가 호출된 이후에 갱신됩니다.
     */
    override fun onGameFullStart() {
        val stopWatch = StopWatch("onGameFullStart")
        stopWatch.start("Map Initializing")
        val mapInitializer = MapInitializer()

        mapInitializer.initializeMapInformation(observation())
        val allTiles = mapInitializer.initializeAllTiles { point ->
            observation().isPathable(point)
        }
        mapInitializer.initializeTilesAdjacency(allTiles)

        Game.allTiles = allTiles    // 반드시 인접한 Tile 들을 갱신한 이후

        mapInitializer.logDetailResult(Level.DEBUG)

        log.info { "mapName: ${Game.mapName}, Size: (${Game.mapWidth}, ${Game.mapHeight}})" }
        stopWatch.stop()

        stopWatch.start("Player Race Initializing")
        val playerInitializer = PlayerInitializer()

        Game.selfId = observation().playerId
        Game.players = observation().gameInfo.playersInfo

        playerInitializer.initializeRace(playerId = Game.selfId) { Game.playerRace = it }
        log.debug { "playerRace: ${Game.playerRace}" }

        val enemyId = { player: PlayerInfo ->
            player.playerType.get() != PlayerType.OBSERVER
                    && player.playerId != Game.selfId
        }
        playerInitializer.initializeRace(playerClause = enemyId) { Game.enemyRace = it }
        log.debug { "enemyRace: ${Game.enemyRace}" }

        playerInitializer.logDetailResult(Level.DEBUG)
        log.info { "PlayerRace: ${Game.playerRace} VS EnemyRace: ${Game.enemyRace}" }
        stopWatch.stop()

        log.info { stopWatch.prettyPrint() }
    }

    /**
     * 플레이어의 유닛이 온전히 생성 완료되었을 때 호출됩니다.
     */
    override fun onUnitCreated(unitInPool: UnitInPool?) {
        val pool = unitInPool ?: return // 일반적으로 null 이 들어오는 경우는 없으나, 혹여 들어올 경우 무시
        val unit = pool.unit()

        Game.addPosition(unit.toGameUnit(), Position.fromPoint3d(unit.position))
        log.trace { "Add player unit: $unit" }
    }

    /**
     * 아군, 적군 가리지 않고 삭제되면 호출된다.
     */
    override fun onUnitDestroyed(unitInPool: UnitInPool?) {
        val pool = unitInPool ?: return
        val unit = pool.unit()

        Game.removePosition(unit.toGameUnit())
        log.trace { "Remove player unit: $unit" }
    }
}
