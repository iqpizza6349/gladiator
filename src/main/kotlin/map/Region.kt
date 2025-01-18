package io.iqpizza.map

/**
 * Region 은 다수의 Tile 을 포함하며, 논리적으로 연결된 영역을 나타냅니다.
 */
data class Region(val tiles: List<Tile>) {

    /**
     * 특정 타일이 Region 내에 포함되는지 확인합니다.
     * 포함 여부는 시작(start)과 종료(end) 범위 내에 있는지로 판단됩니다.
     */
    fun contains(tile: Tile): Boolean = tiles.any {
        it.start.x <= tile.start.x && it.end.x >= tile.end.x &&
                it.start.y <= tile.start.y && it.end.y >= tile.end.y
    }

    /**
     * 특정 위치(Position)가 Region 내에 포함되는지 확인합니다.
     */
    fun isPointInside(point: Position): Boolean = tiles.any {
        point.x >= it.start.x && point.x <= it.end.x &&
                point.y >= it.start.y && point.y <= it.end.y
    }

    /**
     * Region 내 모든 타일의 중심점을 반환합니다.
     */
    fun getAllTileCenters(): List<Position> = tiles.map { it.position }
}