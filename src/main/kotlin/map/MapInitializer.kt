package io.iqpizza.map

import com.github.ocraft.s2client.bot.gateway.ObservationInterface
import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.iqpizza.game.Game
import io.iqpizza.utils.StopWatch
import map.SimpleArea
import map.Territory
import utils.analyzeChokePoints
import utils.generateAllTiles
import utils.initializeAreas

class MapInitializer(private val stopWatch: StopWatch) {
    companion object {
        private val supportsLevel = arrayOf(
            Level.INFO,
            Level.WARN,
            Level.TRACE,
        )
    }

    private val logger = KotlinLogging.logger("MapInitializer-${stopWatch.id}")

    /**
     * MapName, MapHeight, MapWidth 를 초기화하는 메소드입니다.
     */
    fun initializeMapInformation(observation: ObservationInterface) {
        stopWatch.start("Initialize Map Information")
        val gameInfo = observation.gameInfo

        Game.mapName = gameInfo.mapName
        Game.startRaw = gameInfo.startRaw.get()

        val mapSize = Game.startRaw.mapSize
        Game.mapWidth = mapSize.x
        Game.mapHeight = mapSize.y
        stopWatch.stop()
    }

    fun initializeAllTiles(isPathable: ((Point2d) -> Boolean)): List<Tile> {
        checkNotNull(Game.mapWidth)
        checkNotNull(Game.mapHeight)
        stopWatch.start("Initialize All Tiles")

        val allTiles = generateAllTiles(Game.mapWidth, Game.mapHeight) {
            isPathable(it.toPoint2d())
        }
        stopWatch.stop()
        return allTiles
    }

    fun initializeTilesAdjacency(tiles: List<Tile>) {
        stopWatch.start("Initialize Tiles Adjacency")
        Tile.precomputeAdjacency(tiles)
        stopWatch.stop()
    }

    fun initializePlayerRegion(startLocation: Point, bases: MutableSet<SimpleArea>) {
        stopWatch.start("Initialize Player Region")
        bases.add(SimpleArea(Position.fromPoint3d(startLocation), territory = Territory.SELF))
        stopWatch.stop()
    }

    fun initializeEnemiesRegion(bases: MutableSet<SimpleArea>) {
        stopWatch.start("Initialize Enemies Region")
        checkNotNull(Game.startRaw)

        for (enemySpot in Game.startRaw.startLocations) {
            bases.add(
                SimpleArea(Position.fromPoint2d(enemySpot), territory = Territory.ENEMY)
            )
        }

        stopWatch.stop()
    }

    fun initializeAllRegions(bases: MutableSet<SimpleArea>, allRegions: (() -> List<Point>)) {
        stopWatch.start("Initialize All Regions")
        val expansionLocations = allRegions()
        for (expansionSpot: Point in expansionLocations) {
            bases.add(SimpleArea(Position.fromPoint3d(expansionSpot)))
        }

        stopWatch.stop()
    }

    fun initializeAllArea(bases: MutableSet<SimpleArea>) {
        stopWatch.start("Initialize All Area")
        checkNotNull(Game.allRegions)
        Game.expansionAreas = initializeAreas(bases, Game.allRegions)
        stopWatch.stop()
    }

    fun initializeChokePoints(tileMap: Map<Position, Tile>) {
        stopWatch.start("Initialize Choke Points")
        checkNotNull(Game.allRegions)

        Game.chokePoints = analyzeChokePoints(Game.allRegions, { current, goto ->
            val tile = tileMap[goto]
            (tile == null || !tile.walkable)
        }).toList()
        stopWatch.stop()
    }

    fun logDetailResult(level: Level = Level.INFO) {
        when (level) {
            Level.TRACE -> logger.trace { stopWatch.prettyPrint() }
            Level.DEBUG -> logger.debug { stopWatch.prettyPrint() }
            Level.INFO  -> logger.info { stopWatch.prettyPrint() }
            else        -> return
        }
    }

    fun logResult(level: Level = Level.INFO) {
        if (level !in supportsLevel) {
            throw IllegalArgumentException("Level $level is not supported")
        }

        val message = "'${stopWatch.id}' takes ${stopWatch.totalTimeMillis} ms. (Total Tasks: ${stopWatch.taskCount})"

        when (level) {
            Level.TRACE -> logger.trace { message }
            Level.DEBUG -> logger.debug { message }
            Level.INFO  -> logger.info { message }
            else        -> return
        }
    }
}
