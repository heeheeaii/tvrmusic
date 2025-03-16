package com.treevalue.atsor.data

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import com.treevalue.atsor.data.simple.FixedSizeQueue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class DataTest {
    @Test
    fun tTest() {
        val manager = NDManager.newBaseManager()
        val myTensor = Tensor(floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f), longArrayOf(2, 3))
        val ndArrayFromTensor = Tensor.toNDArray(manager, myTensor)
        println("NDArray from Tensor: $ndArrayFromTensor")
        println("NDArray shape: ${ndArrayFromTensor.shape}")
        println("NDArray data type: ${ndArrayFromTensor.dataType}")


        val ndArray = manager.create(floatArrayOf(7f, 8f, 9f, 10f, 11f, 12f), Shape(3, 2))
        val myTensorFromNDArray = Tensor.fromNDArray(ndArray)
        println("Tensor from NDArray: $myTensorFromNDArray")


        val myTensor1 = Tensor(floatArrayOf(1f, 2f), longArrayOf(2))
        val myTensor2 = Tensor(floatArrayOf(1f, 2f), longArrayOf(2))
        val myTensor3 = Tensor(floatArrayOf(1f, 3f), longArrayOf(2))
        println("myTensor1 == myTensor2: ${myTensor1 == myTensor2}")
        println("myTensor1 == myTensor3: ${myTensor1 == myTensor3}")


        val intNDArray = manager.create(intArrayOf(1, 2, 3, 4), Shape(2, 2))


        fun fromNDArrayWithTypeHandling(ndArray: NDArray): Tensor {
            val shape = ndArray.shape
            return when (ndArray.dataType) {
                DataType.FLOAT32 -> Tensor(ndArray.toFloatArray(), shape.shape)
                DataType.INT32 -> Tensor(ndArray.toIntArray().map { it.toFloat() }.toFloatArray(), shape.shape)
                DataType.FLOAT64 -> Tensor(ndArray.toDoubleArray().map { it.toFloat() }.toFloatArray(), shape.shape)

                else -> throw IllegalArgumentException("Unsupported data type: ${ndArray.dataType}")
            }
        }

        val myTensorFromInt = fromNDArrayWithTypeHandling(intNDArray)
        println("Tensor from Int NDArray: $myTensorFromInt")


        manager.close()
    }

    @Test
    fun testEcho() {
        val queue = FixedSizeQueue<Int>(5)

        queue.enqueue(1)
        queue.enqueue(2)
        queue.enqueue(3)
        Assertions.assertEquals(2, queue.get(1))

        queue.set(1, 5)
        Assertions.assertEquals(5, queue.get(1))

        queue.enqueue(4)
        queue.enqueue(5)
        queue.enqueue(6)
        Assertions.assertEquals(5, queue.peek());
        queue.enqueue(7)
        Assertions.assertEquals(3, queue.last());
    }
}
