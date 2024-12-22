package com.example

import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import ai.djl.nn.convolutional.Conv2d.conv2d
import org.junit.jupiter.api.Test

class DjlTest {
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
            ), Shape(2, 1, 5, 5))

        val kernel = manager.create(
            floatArrayOf(
                1f, 0f, -1f,
                1f, 0f, -1f,
                1f, 0f, -1f
            ),Shape(1, 1, 3, 3)
        )

        val conv =conv2d(tensor, kernel, null, Shape(1, 1), Shape(1, 1), Shape(1, 1))
        println("Convolution Output:${conv[0]}")
        println(conv)

        manager.close()
    }
}
