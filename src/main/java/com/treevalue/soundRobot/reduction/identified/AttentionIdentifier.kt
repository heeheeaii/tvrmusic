package com.treevalue.soundRobot.reduction.identified

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex
import ai.djl.ndarray.types.Shape
import com.treevalue.soundRobot.hard.Machine
import java.io.Closeable
import java.util.*
import java.util.concurrent.Executors

class AttentionIdentifier : Closeable {
    private val virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val separateNum = 5f
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

    private fun attentionFilter(tensor: NDArray, part: NDArray, indexStr: String) {
        val k = (part.size() / separateNum).toInt()
        val average = getTopKAverage(part, k, true)
        strengthAttention(part, average)
        clearLessThan(average, part)
        reload(tensor, indexStr, part)
    }

    private fun strengthAttention(part: NDArray, average: Float) {
        part.muli(part.gt(average).muli(1+1/separateNum))
    }

    fun clearBlur(tensor: NDArray) {
        println(tensor)
    }

    fun reload(tensor: NDArray, indexStr: String, processedSubTensor: NDArray) {
        tensor.set(NDIndex(indexStr), processedSubTensor)
    }

    fun clearLessThan(threshold: Float, tensor: NDArray) {
        tensor.set(tensor.lt(threshold), 0.0f)
    }

    fun getTopKAverage(tensor: NDArray, k: Int, decrease: Boolean = true): Float {
        if (k <= 0) {
            return 0f
        }
        val pq = PriorityQueue<Float>()
        tensor.toFloatArray().forEach {
            if (pq.size < k) {
                pq.add(it)
            } else if (it > pq.first()) {
                pq.poll()
                pq.add(it)
            }
        }
        return pq.toFloatArray().average().toFloat()
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
                attentionFilter(tensor, slicedTensor, indexStr)
            } else {
                val parIdx = indices.size
                for (idx in 0 until ranges[parIdx].size) {
                    if (idx == 0) pre[parIdx] = 0
                    if (idx > 0) pre[parIdx] = ranges[parIdx][idx - 1]
                    indices.add(ranges[parIdx][idx])
                    traverse(indices)
                    indices.removeAt(indices.size - 1)
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
