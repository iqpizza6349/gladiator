package io.iqpizza.map

import com.github.ocraft.s2client.protocol.spatial.Point
import com.github.ocraft.s2client.protocol.spatial.Point2d
import kotlin.math.pow
import kotlin.math.sqrt

data class Position(
    val x: Float,
    val y: Float
) {
    fun toPoint2d(): Point2d = Point2d.of(x, y)
    fun toPoint3d(z: Float = 0.0F): Point = Point.of(x, y, z)

    companion object {
        fun fromPoint2d(point: Point2d): Position = Position(point.x, point.y)
        fun fromPoint3d(point: Point): Position = Position(point.x, point.y)
    }

    /**
     * 다른 Position 와 크기를 더해 새로운 Position 를 만듭니다.
     */
    fun add(other: Position): Position = Position(x + other.x, y + other.y)

    /**
     * 현재 Position 에서 다른 Position 크기 만큼 제거해 새로운 Position 를 만듭니다.
     */
    fun sub(other: Position): Position = Position(x - other.x, y - other.y)

    /**
     * 현재 Vector 에서 다른 Vector 까지의 거리를 계산합니다.
     */
    fun distance(other: Position): Float = sqrt((x - other.x).toDouble().pow(2.0)
            + (y - other.y).toDouble().pow(2.0)).toFloat()

    fun distance(other: Point2d): Float = distance(fromPoint2d(other))

    fun distance(other: Point): Float = distance(fromPoint3d(other))
}
