package com.treevalue.atsor.hard

import com.treevalue.atsor.data.Tensor
import com.treevalue.atsor.taskpoll.Task
import com.treevalue.atsor.taskpoll.TaskPool


class SoundTasker {
    val task =
        Task(
            Tensor(FloatArray(5) { 0f }, longArrayOf(5)),
            Tensor(FloatArray(5) { 0f }, longArrayOf(5))
        )

    init {
        TaskPool.queue.push(1, task)
    }
}
