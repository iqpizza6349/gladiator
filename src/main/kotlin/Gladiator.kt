package io.iqpizza

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import io.github.oshai.kotlinlogging.KotlinLogging
import io.iqpizza.game.Game
import io.iqpizza.map.Position
import io.iqpizza.map.Tile
import io.iqpizza.utils.StopWatch
import map.SimpleArea
import map.Territory
import utils.analyzeChokePoints
import utils.generateAllTiles
import utils.generateRegions
import utils.initializeAreas

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

        // 모든 Base(자원이 주변에 존재하는 영역) 들을 포함한 컬렉션
        val bases = mutableSetOf<SimpleArea>()

        // 1. 플레이어 본인의 진영을 등록한다.
        stopWatch.start("Initialize Player Region")
        bases.add(SimpleArea(Position.fromPoint3d(observation().startLocation), territory = Territory.SELF))
        stopWatch.stop()

        // 2. 적 본진이 될 수 있는 진영을 등록한다.
        stopWatch.start("Initialize Enemies Region")
        for (enemySpot: Point2d in Game.startRaw.startLocations) {
            bases.add(
                SimpleArea(
                    Position.fromPoint2d(enemySpot),
                    territory = Territory.ENEMY
                )
            )
        }
        stopWatch.stop()

        // 3. 나머지 모든 진영들 중 적 본진 대상 가능성이 존재하는 영토를 제외하여, 모두 등록한다.
        stopWatch.start("Initialize Left Regions")
        val expansionLocations = query().calculateExpansionLocations(observation())
        for (expansionSpot: Point in expansionLocations) {
            bases.add(SimpleArea(Position.fromPoint3d(expansionSpot)))
        }
        stopWatch.stop()

        stopWatch.start("Validate All Regions")
        val walkableTiles = Game.allTiles.filter { tile -> tile.walkable }
        Game.allRegions = generateRegions(walkableTiles, bases)
        stopWatch.stop()

        stopWatch.start("Initialize All Regions")
        Game.expansionAreas = initializeAreas(bases, Game.allRegions)
        stopWatch.stop()

        stopWatch.start("Initialize and Analyze choke-points")
        val tileMap = Game.allTiles.associateBy { it.position }
        Game.chokePoints = analyzeChokePoints(Game.allRegions, { current, goto ->
            val tile = tileMap[goto]
            (tile == null || !tile.walkable)
        }).toList()
        stopWatch.stop()

        log.info { stopWatch.prettyPrint() }
        log.info { "Total GameStart Takes ${stopWatch.totalTimeMillis} ms" }
    }

    /**
     * 게임이 최초 로드되었을 때 호출되는 메소드입니다.
     * startLocation 갱신은 해당 메소드가 호출된 이후에 갱신됩니다.
     */
    override fun onGameFullStart() {
        val stopWatch = StopWatch("onGameFullStart")
        
        stopWatch.start("Get GameInfo")
        val gameInfo = observation().gameInfo
        stopWatch.stop()

        Game.mapName = gameInfo.mapName
        Game.startRaw = gameInfo.startRaw.get()

        val mapSize = Game.startRaw.mapSize
        Game.mapWidth = mapSize.x
        Game.mapHeight = mapSize.y

        log.info { "mapName: ${Game.mapName}, Size: (${Game.mapWidth}, ${Game.mapHeight}})" }

        stopWatch.start("Initializing Map with Tile")
        val allTiles = generateAllTiles(Game.mapWidth, Game.mapHeight) { v ->
            observation().isPathable(v.toPoint2d())
        }
        stopWatch.stop()

        stopWatch.start("Tile Adjacency")
        // 각 타일들의 인접성들을 일괄로 갱신합니다.
        Tile.precomputeAdjacency(allTiles)
        stopWatch.stop()

        log.info { stopWatch.prettyPrint() }
        log.info { "Total GameFullStart Takes ${stopWatch.totalTimeMillis} ms" }

        Game.allTiles = allTiles
    }
}
