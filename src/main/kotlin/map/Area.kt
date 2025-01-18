package map

import com.github.ocraft.s2client.bot.gateway.UnitInPool
import io.iqpizza.map.Position
import io.iqpizza.map.Region
import io.iqpizza.map.Tile

interface Area {
    val center: Position
    val region: Region

    fun isPointInside(point: Position): Boolean {
        return region.tiles.any {
            it.position.x == point.x && it.position.y == point.y
        }
    }

    fun isAdjacent(point: Position): Boolean {
        // Region 내의 모든 타일의 주변 위치와 point 간 인접 여부를 확인
        return region.tiles.any { tile ->
            val neighbors = getAdjacentPositions(tile.position)
            neighbors.any { it == point }
        }
    }

    // 특정 위치의 인접 타일 좌표를 계산
    fun getAdjacentPositions(position: Position): List<Position> {
        val offsets = listOf(
            Position(-1f, 0f),  // left
            Position(1f, 0f),   // right
            Position(0f, -1f),  // up
            Position(0f, 1f),   // down
            Position(-1f, -1f), // up-left
            Position(1f, -1f),  // up-right
            Position(-1f, 1f),  // down-left
            Position(1f, 1f)    // down-right
        )
        return offsets.map { offset -> position.add(offset) }
    }

    fun getBoundaryTiles(): List<Tile> {
        // 중심으로부터 가장 먼 거리와 가까운 거리 계산
        val maxDistance = region.tiles.maxOf { it.position.distance(center) }
        val tolerance = 0.1f // 허용 오차를 추가하여 너무 좁은 범위를 방지
        return region.tiles.filter { tile ->
            val distance = tile.position.distance(center)
            distance >= maxDistance - tolerance
        }
    }
}

enum class Territory {
    SELF,
    ENEMY,
    NEUTRAL
}

open class RegionArea(
    override val center: Position, override val region: Region,
    open val minerals: List<UnitInPool>, open val gases: List<UnitInPool>,
) : Area

data class TerritoryArea(
    override val center: Position, override val region: Region,
    override val minerals: List<UnitInPool>, override val gases: List<UnitInPool>,
    var territory: Territory = Territory.NEUTRAL
): RegionArea(center, region, minerals, gases) {
    fun isNeutrality(): Boolean = territory == Territory.NEUTRAL

    fun isSelfBase(): Boolean = territory == Territory.SELF

    fun isEnemyBase(): Boolean = territory == Territory.ENEMY
}

data class SimpleArea(
    val center: Position, val territory: Territory = Territory.NEUTRAL, val radius: Float = 9F
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleArea) return false
        return this.center == other.center
    }

    override fun hashCode(): Int = center.hashCode()
}