package com.treevalue.atsor.taskpoll

import com.treevalue.atsor.data.Tensor

data class Task(val from: Tensor, val to: Tensor) {
    fun onSolve() {
    }

    fun onDiscard() {

    }
}
