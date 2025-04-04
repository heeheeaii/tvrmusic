package com.treevalue.atsor.taskpoll

import com.treevalue.atsor.data.simple.PriorityQueue

class TaskPool {
    companion object {
        val queue = PriorityQueue<Task>()
    }
}
