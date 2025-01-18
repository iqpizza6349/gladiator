package io.iqpizza

import com.github.ocraft.s2client.bot.S2Agent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.iqpizza.game.Game
import io.iqpizza.map.MapInitializer
import io.iqpizza.utils.StopWatch
import map.SimpleArea
import utils.generateRegions

class Gladiator : S2Agent() {
    companion object {
        private val log = KotlinLogging.logger("Gladiator")
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
        val mapInitializer = MapInitializer(stopWatch)

        mapInitializer.initializeMapInformation(observation())
        val allTiles = mapInitializer.initializeAllTiles { point ->
            observation().isPathable(point)
        }
        mapInitializer.initializeTilesAdjacency(allTiles)

        Game.allTiles = allTiles    // 반드시 인접한 Tile 들을 갱신한 이후

        mapInitializer.logDetailResult()
        mapInitializer.logResult()

        log.info { "mapName: ${Game.mapName}, Size: (${Game.mapWidth}, ${Game.mapHeight}})" }
    }
}
