package com.treevalue.quick

import org.nd4j.linalg.api.ndarray.INDArray

data class Memory(
    val output: MutableMap<Position, INDArray>,
    var reinforcementCount: Int = 0
) {
    var updateTime: Float = 0f
    var isLongTerm: Boolean = false
    var isFixed: Boolean = false

    fun rememberLonRemember() {
        isLongTerm = true
    }

    fun rememberForever() {
        isFixed = true
    }

    override fun toString(): String {
        return "Memory(outputs=${output.size}, longTerm=$isLongTerm, reinforced=$reinforcementCount, fixed=$isFixed)"
    }
}
