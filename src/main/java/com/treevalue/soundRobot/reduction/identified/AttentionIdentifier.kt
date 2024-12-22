package com.treevalue.soundRobot.reduction.identified

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import com.treevalue.soundRobot.hard.Machine
import kotlinx.coroutines.yield
import java.io.Closeable
import java.util.concurrent.Executors

class AttentionIdentifier : Closeable {
    private val virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val separateNum = 5
    private val coreNum = Machine.getNumberOfCores()
    private val minAttention = 64

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

    fun attentionFilter(tensor: NDArray, indexStr: String) {
        println(indexStr)
        println(tensor)
    }

    private fun firstIdentifier(tensor: NDArray) {
        val shape = tensor.shape.shape
        val steps = shape.map { if (it / separateNum >= minAttention) it / separateNum else it }
        val pre = shape.map { 0 }.toIntArray()

        val ranges = shape.mapIndexed { idx, vle ->
            val list = mutableListOf<Int>()
            val step = steps[idx].toInt()
            val limit = vle.toInt()

            for (itm in (step - 1) until limit step step) {
                list.add(itm)
            }

            if (list.isNotEmpty() && list.last() != limit) {
                if ((limit - list.last()) >= (step / 2)) {
                    list.add(limit)
                } else if (list.isNotEmpty()) {
                    list[list.size - 1] = limit
                }
            }

            list
        }

        fun traverse(indices: MutableList<Int>) {
            if (indices.size == ranges.size) {
                val indexStr = indices.mapIndexed { idx, vle -> "${pre[idx]}:${vle}" }.joinToString(",")
                val slicedTensor = tensor.get(NDIndex(indexStr))
                attentionFilter(slicedTensor, indexStr)
            } else {
                val index = indices.size
                for (num in ranges[index]) {
                    if (index == 0) pre[index] = 0
                    indices.add(num)
                    traverse(indices)
                    indices.removeAt(indices.size - 1)
                    if (index != 1) pre[index] = num
                }
            }
        }
        traverse(mutableListOf())
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val manager = NDManager.newBaseManager()
            val t1 = manager.randomNormal(Shape(320, 320, 15))
            AttentionIdentifier().firstIdentifier(t1)
        }
    }
}
