package com.treevalue.atsor.reduction.identified

import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import org.junit.jupiter.api.Test

class AttentionTest {

    @Test
    fun identifier() {
        var manager = NDManager.newBaseManager()
        var randTsr = manager.randomInteger(0, 255, Shape(64, 64), DataType.INT8)
        var get = Attention().identifier(randTsr, manager)
    }
}
