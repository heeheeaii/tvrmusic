package com.treevalue.quick

import java.util.UUID
import kotlin.math.exp

/**
 * 全量生长过程中，相邻两个神经元之间的生长
 */
data class MiddleGrowthEvent(
    val parentProcessId: UUID,
    val sourcePosition: Position,
    val targetPosition: Position
) {
    private val totalDistance: Float = Position.distanceOf(sourcePosition, targetPosition)
    private var progress: Float = 0f
    private var reinforcementCount: Int = 1
    private var speed: Float = calculateSpeed()

    fun reinforce() {
        reinforcementCount++
        speed = calculateSpeed()
    }

    private fun calculateSpeed(): Float {
        val baseSpeed = 1.0f
        val maxTotalSpeed = 10.0f
        val maxBonusSpeed = maxTotalSpeed - baseSpeed
        val k = 0.5f
        val r0 = 10.0f
        val multi = 1.5f
        val logisticBonus = maxBonusSpeed / (1 + exp(-k * (multi * reinforcementCount - r0)))
        return baseSpeed + logisticBonus
    }

    fun advanceThenArrive(): Boolean {
        progress += speed
        return progress >= totalDistance
    }
}
