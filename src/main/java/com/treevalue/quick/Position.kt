package com.treevalue.quick

import java.io.Serializable
import kotlin.math.sqrt

data class Position(val x: Float, val y: Float, val z: Float) : Serializable {
    companion object {
        fun distanceOf(from: Position, other: Position): Float {
            val dx = from.x - other.x
            val dy = from.y - other.y
            val dz = from.z - other.z
            return sqrt(dx * dx + dy * dy + dz * dz)
        }
    }
}
