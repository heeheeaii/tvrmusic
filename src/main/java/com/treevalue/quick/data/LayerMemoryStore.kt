package com.treevalue.quick.data

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.ops.transforms.Transforms
import java.util.*

typealias LayerMemory = Array<Array<INDArray>>

class LayerMemoryStore(
    // 单个张量之间被认为是相似的阈值
    private val tensorSimilarityThreshold: Double = 0.99,
    // 两个矩阵之间被认为是相似的阈值
    private val matrixSimilarityThreshold: Double = 0.95
) {
    // 使用 LinkedList 存储矩阵，方便在头部进行添加和删除操作，实现MRU（Most Recently Used）特性
    private val storedMatrices: LinkedList<LayerMemory> = LinkedList()

    /**
     * 函数：比较两个一维张量是否相似。
     * 这是一个可定制的部分；实际实现取决于对张量“相似性”的定义。
     * @param t1 第一个张量
     * @param t2 第二个张量
     * @return 如果张量相似则返回 true，否则返回 false
     */
    private fun areTensorsSimilar(t1: INDArray, t2: INDArray): Boolean {
        if (!t1.shape().contentEquals(t2.shape())) {
            return false
        }
        // return t1.equals(t2) // 精确匹配
        // return t1.equalsWithEps(t2, 1e-5) // 考虑浮点数误差的近似匹配
        val euclidean = Transforms.euclideanDistance(t1, t2)
        val maxLength = maxOf(t1.norm2Number().toDouble(), t2.norm2Number().toDouble())
        val threshold = maxLength * (1 - tensorSimilarityThreshold)
        return euclidean < threshold
    }

    private fun areMatricesSimilar(m1: LayerMemory, m2: LayerMemory): Boolean {
        val rows1 = m1.size
        val cols1 = if (rows1 > 0) m1[0].size else 0

        val rows2 = m2.size
        val cols2 = if (rows2 > 0) m2[0].size else 0

        if (rows1 != rows2 || cols1 != cols2) {
            return false
        }

        val rows = rows1
        val cols = cols1

        if (rows == 0 || cols == 0) {
            return true
        }

        val totalTensors = rows * cols
        var similarTensorCount = 0

        for (i in 0 until rows) {
            // 假设 m1 和 m2 都是规范的矩形矩阵
            // 为防止不规则数组（jagged array）导致错误，可以添加检查
            if (m1[i].size != cols || m2[i].size != cols) {
                // 检测到不规则数组，这通常不符合矩阵的定义，视为结构不相似
                System.err.println("Warning: Irregular arrays are encountered when matrix comparison.")
                return false
            }
            for (j in 0 until cols) {
                if (areTensorsSimilar(m1[i][j], m2[i][j])) {
                    similarTensorCount++
                }
            }
        }
        return (similarTensorCount.toDouble() / totalTensors) >= matrixSimilarityThreshold
    }

    /**
     * 检查给定矩阵是否与任何已存储的矩阵相似。
     * 如果找到相似的，则将该已存储的矩阵移动到列表前端（最近使用）。
     * 如果未找到，则将新矩阵添加到列表前端。
     *
     * @param newMatrix 要检查并可能添加的矩阵。
     * @return 如果找到，则返回存储中的相似矩阵（现位于前端）；
     *         否则返回新添加的矩阵本身（也位于前端）。
     */
    fun findAndPromoteOrAdd(newMatrix: LayerMemory): LayerMemory {
        val iterator = storedMatrices.iterator()
        var foundMatrix: LayerMemory? = null

        while (iterator.hasNext()) {
            val currentMatrix = iterator.next()
            if (areMatricesSimilar(currentMatrix, newMatrix)) {
                foundMatrix = currentMatrix
                iterator.remove()
                break
            }
        }

        return if (foundMatrix != null) {
            storedMatrices.offerFirst(foundMatrix)
            foundMatrix
        } else {
            storedMatrices.offerFirst(newMatrix)
            newMatrix
        }
    }

    /**
     * 获取存储的矩阵列表，按最近使用顺序列出。
     * @return 包含所有存储矩阵的列表副本
     */
    fun getAllMatrices(): List<LayerMemory> {
        return storedMatrices.toList()
    }

    /**
     * 获取当前存储的大小。
     * @return 存储中矩阵的数量
     */
    fun size(): Int {
        return storedMatrices.size
    }
}
