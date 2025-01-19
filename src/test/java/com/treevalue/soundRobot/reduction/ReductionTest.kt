package com.treevalue.soundRobot.reduction

import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Random
import kotlin.concurrent.thread

class ReductionTest {

    private lateinit var manager: NDManager

    @BeforeEach
    fun setUp() {
        manager = NDManager.newBaseManager()
    }

    @AfterEach
    fun tearDown() {
        manager.close()
    }


    @Test
    fun testReduction() {
        val reduction = Reduction(manager)
        val random = Random()
        val producerThread = thread {
            for (i in 0 until 15000) {
                val data = FloatArray(10) { random.nextFloat() }
                val tensor = manager.create(data, Shape(10))
                reduction.accept(tensor)
                Thread.sleep(1)
            }
        }

        producerThread.join()
        val consumerThread = thread {
            Thread.sleep(50)
            var count = 0
            val iterator = reduction.findBack()
            while (iterator.hasNext()) {
                iterator.next()
                count++
            }
            println("Consumed $count tensors")
        }
        consumerThread.join()
    }


    @Test
    fun testConcurrentAccess() {
        val reduction = Reduction(manager)
        val random = Random()
        val numThreads = 5
        val tensorsPerThread = 1000

        val threads = mutableListOf<Thread>()
        for (i in 0 until numThreads) {
            threads.add(thread {
                for (j in 0 until tensorsPerThread) {
                    val data = FloatArray(10) { random.nextFloat() }
                    val tensor = manager.create(data, Shape(10))
                    reduction.accept(tensor)
                }
            })
        }

        threads.forEach { it.join() }

        val iterator = reduction.findBack()
        var count = 0
        while (iterator.hasNext()) {
            iterator.next()
            count++
        }

        println("Total tensors consumed: $count")
        assert(count <= numThreads * tensorsPerThread)
    }

    @Test
    fun testEmptyBuffer() {
        val reduction = Reduction(manager)
        val iterator = reduction.findBack()
        reduction.accept(manager.create(1f))
        assert(iterator.hasNext())
        val v1 = iterator.next()
        assert(v1.toFloatArray()[0] == 1.0f)
        assert(!iterator.hasNext())
    }

}
