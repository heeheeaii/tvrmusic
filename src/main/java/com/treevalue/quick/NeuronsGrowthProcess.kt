package com.treevalue.quick

import java.util.UUID

/**
 * Manages the state of an entire multi-segment growth path from an initial source to a final target.
 */
internal data class NeuronsGrowthProcess(
    val id: UUID = UUID.randomUUID(),
    val fullPath: List<Position>,
    val initialNeuronPosition: Position
) {
    private var currentPathIndex: Int = 0

    fun isComplete(): Boolean = currentPathIndex >= fullPath.size

    fun getNextPositionStart(): Position {
        return if (currentPathIndex == 0) {
            initialNeuronPosition
        } else {
            fullPath[currentPathIndex - 1]
        }
    }

    fun getNextPositionTarget(): Position = fullPath[currentPathIndex]

    fun advanceToNext() {
        if (!isComplete()) {
            currentPathIndex++
        }
    }
}
