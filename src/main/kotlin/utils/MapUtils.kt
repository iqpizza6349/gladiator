package utils

import io.iqpizza.map.Position
import io.iqpizza.map.Region
import io.iqpizza.map.Tile
import map.RegionArea
import map.SimpleArea
import map.TerritoryArea
import java.util.*
import kotlin.math.*

fun generateAllTiles(mapWidth: Int, mapHeight: Int, isWalkable: (Position) -> Boolean): List<Tile> {
    val tiles = mutableListOf<Tile>()
    for (y in 0 until mapHeight) {
        for (x in 0 until mapWidth) {
            val start = Position(x.toFloat(), y.toFloat())
            val end = Position((x + 1).toFloat(), (y + 1).toFloat())
            val center = getCenterPoint(start, end)

            tiles.add(Tile(start = start, end = end, walkable = isWalkable(center)))
        }
    }

    return tiles
}

fun getCenterPoint(source: Position, desc: Position): Position {
    return Position((source.x + desc.x) / 2, (source.y + desc.y) / 2)
}

fun generateRegions(
    tiles: Collection<Tile>,                 // 전체 타일 리스트
    expansionAreas: Collection<SimpleArea> // ExpansionArea 리스트
): List<Region> {
    val regions = mutableListOf<Region>()
    val visited = mutableSetOf<Tile>()

    // 1. ExpansionArea 기반 Region 생성
    expansionAreas.forEach { expansion ->
        val regionTiles = tiles.filter { tile ->
            tile.position.distance(expansion.center) <= expansion.radius // Expansion 범위 내 타일
        }

        visited.addAll(regionTiles) // 방문 처리
        regions.add(Region(regionTiles))
    }

    /**
     * 인접 타일을 재귀적으로 탐색하며 연결된 타일을 하나의 Region 으로 정의.
     * 특정 타일에서 시작해 해당 타일과 연결된 모든 타일을 탐색하여 Region 생성.
     */
    // 2. 나머지 타일 기반 Region 생성 (Flood Fill)
    fun floodFill(startTile: Tile): Region {
        val queue: Queue<Tile> = LinkedList()
        val regionTiles = mutableSetOf<Tile>()
        queue.add(startTile)

        while (queue.isNotEmpty()) {
            val current = queue.poll()

            if (current !in visited && current.walkable) {
                visited.add(current)
                regionTiles.add(current)

                // 상하좌우 인접 타일 탐색
                queue.addAll(current.adjacentTiles.filter { neighbor ->
                    neighbor !in visited && neighbor.walkable
                })
            }
        }

        return Region(regionTiles.toMutableList())
    }

    // 나머지 타일에 대해 Flood Fill 수행
    tiles.filter { it !in visited }.forEach { tile ->
        regions.add(floodFill(tile))
    }

    return regions
}

fun initializeAreas(
    expansions: Collection<SimpleArea>,
    regions: Collection<Region>
): List<RegionArea> {
    val areas = mutableListOf<RegionArea>()

    // 2. ExpansionArea 기반 Area 생성
    expansions.forEach { expansion ->
        // ExpansionArea 와 일치하는 Region 찾기
        val matchingRegion = regions.find { region ->
            region.tiles.any { tile ->
                tile.position.distance(expansion.center) <= expansion.radius
            }
        }

        // Area 생성 및 추가
        if (matchingRegion != null) {
            areas.add(
                TerritoryArea(
                    center = expansion.center,
                    region = matchingRegion,
                    minerals = emptyList(),  // 필요 시 자원 데이터 연결
                    gases = emptyList(),     // 필요 시 가스 데이터 연결
                    territory = expansion.territory
                )
            )
        }
    }
    return areas
}

fun Int.toRadians(): Float = this * (PI.toFloat() / 180)

fun rayCast(
    start: Position,
    direction: Position,
    maxDistance: Float = 100F,
    isWalkable: (Position, Position) -> Boolean
): Float {
    val stepX = if (direction.x > 0) 1 else -1
    val stepY = if (direction.y > 0) 1 else -1

    val deltaX = abs(1 / direction.x)
    val deltaY = abs(1 / direction.y)

    var nextX = if (direction.x > 0) {
        (ceil(start.x) - start.x) * deltaX
    } else {
        (start.x - floor(start.x)) * deltaX
    }

    var nextY = if (direction.y > 0) {
        (ceil(start.y) - start.y) * deltaY
    } else {
        (start.y - floor(start.y)) * deltaY
    }

    var currentX = start.x
    var currentY = start.y
    var distance = 0F

    while (distance < maxDistance) {
        val prevX = currentX
        val prevY = currentY

        if (nextX < nextY) {
            currentX += stepX
            distance = nextX
            nextX += deltaX
        } else {
            currentY += stepY
            distance = nextY
            nextY += deltaY
        }

        // 비정상적인 경우, 무시하도록 한다.
        if (prevX < 0 || prevY < 0 || currentX < 0 || currentY < 0) {
            return distance
        }

        if (!isWalkable(Position(prevX, prevY), Position(currentX, currentY))) {
            return distance
        }
    }

    return maxDistance
}

fun detectChokePoints(
    tiles: List<Tile>,
    isWalkable: (Position, Position) -> Boolean,
    maxDistance: Float = 100F,
    edgeThreshold: Float = 2.2F // Edge 로 간주할 거리
): List<Position> {
    val chokePoints = mutableListOf<Position>()

    tiles.forEach { tile ->
        val position = tile.position
        var currentAngle = 270

        while (currentAngle < 360) {
            // Group 1: 0도, 180도
            val group1Distances = listOf(
                rayCast(position, Position(1f, 0f).rotate(currentAngle), maxDistance, isWalkable),
                rayCast(position, Position(-1f, 0f).rotate(currentAngle), maxDistance, isWalkable)
            ).sum()

            // Group 2: 90도, 270도
            val group2Distances = listOf(
                rayCast(position, Position(0f, 1f).rotate(currentAngle), maxDistance, isWalkable),
                rayCast(position, Position(0f, -1f).rotate(currentAngle), maxDistance, isWalkable)
            ).sum()

            // Edge 조건 추가: 특정 방향 거리가 edgeThreshold 이하인 경우
            if (group1Distances / 2 < edgeThreshold || group2Distances / 2 < edgeThreshold) {
                break // edge 타일이므로 ChokePoint 로 간주하지 않음
            }

            // ChokePoint 필터링
            if (isChokePoint(group1Distances, group2Distances)) {
                chokePoints.add(position)
                break // 중복 제거
            }

            currentAngle += 5 // 회전 각도 갱신
        }
    }

    return chokePoints
}

fun isChokePoint(group1: Float, group2: Float): Boolean {
    return (group1 < 8 && group2 < 8 && abs(group1 - group2) > 1) &&
            !(group1 < 1 && group2 < 1)
}

fun Position.rotate(degrees: Int): Position {
    val radians = degrees.toRadians()
    val cos = cos(radians)
    val sin = sin(radians)
    return Position(
        x * cos - y * sin,
        x * sin + y * cos
    )
}

fun analyzeChokePoints(
    regions: List<Region>,
    isWalkable: (Position, Position) -> Boolean,
    maxDistance: Float = 100F
): Set<Position> {
    val chokePoints = mutableSetOf<Position>()

    regions.forEach { region ->
        chokePoints += detectChokePoints(region.tiles, isWalkable, maxDistance)
    }
    return chokePoints
}