package com.treevalue.atsor.data

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class TensorMatrixStoreTest {

    private lateinit var store: TensorMatrixStore

    private fun createTensor(vararg values: Double): INDArray {
        return Nd4j.create(values)
    }

    private fun createMatrix(rows: Int, cols: Int, valueOffset: Double = 0.0): TensorMatrix {
        return Array(rows) { r ->
            Array(cols) { c ->
                createTensor((r * cols + c + 1 + valueOffset), (r * cols + c + 2 + valueOffset))
            }
        }
    }

    @BeforeEach
    fun setup() {
        store = TensorMatrixStore(tensorSimilarityThreshold = 0.99, matrixSimilarityThreshold = 0.95)
    }

    @Test
    fun `test adding and retrieving matrices`() {
        val matrix1 = createMatrix(2, 2, 0.0)
        val matrix2 = createMatrix(2, 2, 10.0)

        store.findAndPromoteOrAdd(matrix1)
        assertEquals(1, store.size())
        assertArrayEquals(matrix1, store.getAllMatrices().first())

        store.findAndPromoteOrAdd(matrix2)
        assertEquals(2, store.size())
        assertArrayEquals(matrix2, store.getAllMatrices().first())
        assertArrayEquals(matrix1, store.getAllMatrices()[1])
    }

    @Test
    fun `test similar matrix promotes instead of adds`() {
        val matrix1 = createMatrix(2, 2, 0.0)
        store.findAndPromoteOrAdd(matrix1)

        // matrix1Similar: 只有一个张量略微不同
        val matrix1Similar = Array(2) { r ->
            Array(2) { c ->
                if (r == 0 && c == 0) {
                    createTensor(1.00001, 2.00001)
                } else {
                    createTensor((r * 2 + c + 1).toDouble(), (r * 2 + c + 2).toDouble())
                }
            }
        }
        store.findAndPromoteOrAdd(matrix1Similar)

        // 应该没有新增，只是matrix1被提升到最前面
        assertEquals(1, store.size())
        assertArrayEquals(matrix1, store.getAllMatrices().first())
    }

    @Test
    fun `test partially similar matrix adds new entry`() {
        val matrix1 = createMatrix(2, 2, 0.0)
        store.findAndPromoteOrAdd(matrix1)

        // matrix1PartiallySimilar: 只有1个张量不同
        val matrix1PartiallySimilar = Array(2) { r ->
            Array(2) { c ->
                if (r == 1 && c == 1) {
                    createTensor(99.0, 100.0)
                } else {
                    createTensor((r * 2 + c + 1).toDouble(), (r * 2 + c + 2).toDouble())
                }
            }
        }
        store.findAndPromoteOrAdd(matrix1PartiallySimilar)

        // 新增了一个
        assertEquals(2, store.size())
        assertArrayEquals(matrix1PartiallySimilar, store.getAllMatrices().first())
        assertArrayEquals(matrix1, store.getAllMatrices()[1])
    }

    @Test
    fun `test promoting old matrix to front`() {
        val matrix1 = createMatrix(2, 2, 0.0)
        val matrix2 = createMatrix(2, 2, 10.0)
        store.findAndPromoteOrAdd(matrix1)
        store.findAndPromoteOrAdd(matrix2)
        assertArrayEquals(matrix2, store.getAllMatrices().first())

        // 再添加matrix1的副本，应将其提升到前端，不增加数量
        val matrix1Copy = createMatrix(2, 2, 0.0)
        store.findAndPromoteOrAdd(matrix1Copy)
        assertEquals(2, store.size())
        assertArrayEquals(matrix1, store.getAllMatrices().first())
    }

    @Test
    fun `test empty matrix handling`() {
        val emptyMatrix1: TensorMatrix = emptyArray()
        val emptyMatrix2: TensorMatrix = emptyArray()
        store.findAndPromoteOrAdd(emptyMatrix1)
        assertEquals(1, store.size())
        assertArrayEquals(emptyMatrix1, store.getAllMatrices().first())
        // 再添加另一个空矩阵，不会新增
        store.findAndPromoteOrAdd(emptyMatrix2)
        assertEquals(1, store.size())
        assertArrayEquals(emptyMatrix2, store.getAllMatrices().first())
    }

    @Test
    fun `test zero rows and zero cols matrices are not similar`() {
        val zeroRowsMatrix: TensorMatrix = Array(0) { Array(2) { createTensor(1.0) } }
        val zeroColsMatrix: TensorMatrix = Array(2) { Array(0) { createTensor(1.0) } }
        store.findAndPromoteOrAdd(zeroRowsMatrix)
        assertEquals(1, store.size())
        assertArrayEquals(zeroRowsMatrix, store.getAllMatrices().first())

        store.findAndPromoteOrAdd(zeroColsMatrix)
        assertEquals(2, store.size())
        assertArrayEquals(zeroColsMatrix, store.getAllMatrices().first())
    }
}
