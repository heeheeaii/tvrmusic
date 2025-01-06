package com.treevalue.soundRobot.reduction.identified

import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class AttentionIdentifierTest {

    @Test
    fun identifier() {
        var manager = NDManager.newBaseManager()
        var randTsr = manager.randomInteger(0, 255, Shape(64, 64), DataType.INT8)
        var get = AttentionIdentifier().identifier(randTsr, manager)
    }
}
