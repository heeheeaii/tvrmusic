package com.example

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import ai.djl.nn.convolutional.Conv2d.conv2d
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout

class DjlTest {
    val managerInner: NDManager = NDManager.newBaseManager()

    @Test
    fun test() {
        val manager: NDManager = NDManager.newBaseManager()

        val tensor = manager.create(
            floatArrayOf(
                1f, 2f, 3f, 4f, 5f,
                6f, 7f, 8f, 9f, 10f,
                11f, 12f, 13f, 14f, 15f,
                16f, 17f, 18f, 19f, 20f,
                21f, 22f, 23f, 24f, 25f,
                1f, 2f, 3f, 4f, 5f,
                6f, 7f, 8f, 9f, 10f,
                11f, 12f, 13f, 14f, 15f,
                16f, 17f, 18f, 19f, 20f,
                21f, 22f, 23f, 24f, 25f
            ), Shape(2, 1, 5, 5)
        )

        val kernel = manager.create(
            floatArrayOf(
                1f, 0f, -1f,
                1f, 0f, -1f,
                1f, 0f, -1f
            ), Shape(1, 1, 3, 3)
        )

        val conv = conv2d(tensor, kernel, null, Shape(1, 1), Shape(1, 1), Shape(1, 1))
        println("Convolution Output:${conv[0]}")
        println(conv)

        manager.close()
    }

    @Test
    fun maskTest() {
        NDManager.newBaseManager().use { manager ->
            val array: NDArray = manager.create(floatArrayOf(1f, 5f, 3f, 7f, 2f))
            val threshold = 4f
            val mask: NDArray = array.gt(threshold)
            val m1 = mask.mul(2)
            val m2 = m1.eq(0)
            array.muli(m1.add(m2))
            println(array)
        }
    }
    @Test
    fun inSpace(){
        NDManager.newBaseManager().use { manager ->
            val array1 = manager.create(floatArrayOf(1f, 2f, 3f))
            val array2 = manager.create(floatArrayOf(1f, 2f, 3f))
            val array3 = manager.create(floatArrayOf(1f, 2f, 4f))
            val isEqual12 = array1.eq(array2).all().getBoolean()
            val isEqual13 = array1.eq(array3).all().getBoolean()
            assert(isEqual12)
            assert(!isEqual13)
        }
        val t = managerInner.create(floatArrayOf(1f,2f,3f))
        val t2 = managerInner.create(floatArrayOf(2f,4f,6f))
        assert(t.muli(2).eq(t2).all().getBoolean())
    }

}
