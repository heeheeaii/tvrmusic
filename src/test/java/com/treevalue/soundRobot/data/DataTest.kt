package com.treevalue.soundRobot.data

import com.treevalue.soundRobot.data.simple.FixedSizeQueue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class DataTest {
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
