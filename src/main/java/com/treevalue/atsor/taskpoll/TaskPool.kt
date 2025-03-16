package com.treevalue.atsor.taskpoll

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import com.treevalue.atsor.data.Tensor
import java.util.concurrent.ConcurrentLinkedQueue

class TaskPool {
    val queue = ConcurrentLinkedQueue<Task>()

    init {
        queue.add(
            Task(
                Tensor(FloatArray(5, { _ -> 0f }), LongArray(1, { _ -> 5 })),
                Tensor(FloatArray(5, { _ -> 0f }), LongArray(1, { _ -> 5 }))
            )
        )
    }

    fun putTask(task: Task) {
    }
}
