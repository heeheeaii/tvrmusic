package com.treevalue.atsor.statemachine

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class StateTensor<T>(var shape: List<Int>, var data: MutableList<T>) {
    val size: Int = shape.reduce { d1, d2 -> d1 * d2 }

    private val lock = ReentrantReadWriteLock()

    private fun calculateIndex(indices: IntArray): Int {
        var index = 0
        var multiplier = 1
        for (i in indices.indices.reversed()) {
            index += indices[i] * multiplier
            multiplier *= shape[i]
        }
        return index
    }

    operator fun get(vararg indices: Int): T {
        require(indices.size == shape.size) { "Number of indices must match the tensor's shape" }
        val index = calculateIndex(indices)

        lock.read {
            return data[index]
        }
    }

    operator fun set(vararg indices: Int, value: T) {
        require(indices.size == shape.size) { "Number of indices must match the tensor's shape" }
        val index = calculateIndex(indices)

        lock.write {
            data[index] = value
        }
    }
}
