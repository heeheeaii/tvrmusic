package com.treevalue.soundRobot.reduction

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import com.treevalue.soundRobot.hard.TensorManager
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Reduction(private val manage: NDManager = TensorManager.getManager()) {
    private val period = 2e4 // 20ms
    private val maxSize = 12000
    val memory = LinkedHashMap<Long, NDArray>(1024)
    private val lock = ReentrantReadWriteLock()
    private var lastKey: Long? = null

    companion object {
        var preTime = 0 // us
    }

    fun accept(tensor: NDArray): Unit {
        val now = System.currentTimeMillis() * 1000 + System.nanoTime() / 1000
        if (now - preTime < period) {
            return
        }
        lock.write {
            if (memory.size >= maxSize) {
                memory.remove(memory.keys.first())
            }
            memory[now] = tensor
        }
    }


    fun findBack(): Iterator<NDArray> {
        lock.read {
            if (lastKey == null) {
                lastKey = memory.keys.lastOrNull()
            }
        }
        return object : Iterator<NDArray> {
            private var currentKey: Long? = lastKey
            override fun hasNext(): Boolean {
                return currentKey != null
            }

            override fun next(): NDArray {
                lock.read {
                    val value = memory[currentKey]
                    if (value == null) {
                        currentKey = null
                        return manage.zeros(Shape(0))
                    }
                    val currentIndex = memory.keys.indexOf(currentKey)
                    currentKey = if (currentIndex > 0) {
                        memory.keys.elementAtOrNull(currentIndex - 1)
                    }else{
                        null
                    }
                    return value
                }
            }
        }
    }
}
