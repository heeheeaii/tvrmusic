package com.treevalue.quick

import java.io.Serializable
import kotlin.math.hypot
import kotlin.math.sqrt

typealias Point = Position

data class Position(val x: Int, val y: Int, val z: Int) : Serializable {
    companion object {
        fun distanceOf(from: Position, other: Position): Float {
            val dx = from.x - other.x
            val dy = from.y - other.y
            val dz = from.z - other.z
            return sqrt(dx * dx.toDouble() + dy * dy + dz * dz).toFloat()
        }
    }

    fun distanceTo(o: Position): Float {
        val dx = x - o.x
        val dy = y - o.y
        val dz = z - o.z
        return sqrt(dx * dx.toDouble() + dy * dy + dz * dz).toFloat()
    }

    override fun toString() = "P(L$z, R$y, C$x)"
}
