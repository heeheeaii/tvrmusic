package com.treevalue.soundRobot.reduction.identified

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import com.treevalue.soundRobot.hard.Machine
import java.io.Closeable
import java.util.concurrent.Executors

class Identifier : Closeable {
    private val virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val filterThreshold = 0.2f
    private val coreNum = Machine.getNumberOfCores()

    fun identifier(tensor: NDArray, manager: NDManager): NDArray {
        val copy = manager.create(tensor.shape)
        tensor.copyTo(copy)
        firstIdentifier(copy)
        secondIdentifier(copy)
        thirdIdentifier(copy)
        firstClear(copy)
        secondClear(copy)
        markTensor(copy)
        return copy
    }

    private fun markTensor(tensor: NDArray) {
        TODO("Not yet implemented")
    }

    private fun secondClear(tensor: NDArray) {
        TODO("Not yet implemented")
    }

    private fun firstClear(tensor: NDArray) {
        TODO("Not yet implemented")
    }

    private fun thirdIdentifier(tensor: NDArray) {
        TODO("Not yet implemented")
    }

    private fun secondIdentifier(tensor: NDArray) {
        TODO("Not yet implemented")
    }

    private fun firstIdentifier(tensor: NDArray) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}
