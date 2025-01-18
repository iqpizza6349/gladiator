package io.iqpizza.map

/**
 * 한 Tile 은 1x1 을 나타내는 단위이다.
 * 맵의 해상도에 따른 1x1 인 구조이며, 예를 들어 128x128 이라면
 * 총 16384 개의 Tile 이 만들어지는 구조입니다.
 */
data class Tile(
    val start: Position, // 타일의 시작점
    val end: Position,   // 타일의 종료점
    val walkable: Boolean = false
) {
    val position: Position
        get() = Position(
            (start.x + end.x) / 2, // 중심 x
            (start.y + end.y) / 2  // 중심 y
        )

    // 타일의 크기
    val width: Float
        get() = end.x - start.x

    val height: Float
        get() = end.y - start.y

    val adjacentTiles: MutableList<Tile> = mutableListOf()

    companion object {
        fun precomputeAdjacency(tiles: List<Tile>) {
            val tileMap = tiles.associateBy { Pair(it.position.x.toInt(), it.position.y.toInt()) }
            tiles.forEach { tile ->
                val x = tile.position.x.toInt()
                val y = tile.position.y.toInt()
                val neighbors = listOf(
                    Pair(x - 1, y), Pair(x + 1, y), Pair(x, y - 1), Pair(x, y + 1),
                    Pair(x - 1, y - 1), Pair(x + 1, y - 1), Pair(x - 1, y + 1), Pair(x + 1, y + 1)
                )
                neighbors.mapNotNull { tileMap[it] }.forEach { neighbor ->
                    tile.adjacentTiles.add(neighbor)
                }
            }
        }
    }
}
