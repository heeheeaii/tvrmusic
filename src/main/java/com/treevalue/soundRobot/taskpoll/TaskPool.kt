package com.treevalue.soundRobot.taskpoll

import ai.djl.ndarray.NDArray
import java.util.concurrent.ConcurrentLinkedQueue

class TaskPool {
    val queue = ConcurrentLinkedQueue<Task>()
    fun putTask(task: Task) {
        TODO()
    }
}
