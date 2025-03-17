package com.treevalue.atsor.taskpoll

import com.treevalue.atsor.data.Tensor
import com.treevalue.atsor.data.simple.PriorityQueue
import java.util.concurrent.ConcurrentLinkedQueue

class TaskPool {
    companion object {
        val queue = PriorityQueue<Task>()
    }
}
