package com.treevalue.soundRobot.taskpoll

import ai.djl.ndarray.NDArray

data class Task(val from: NDArray, val to: NDArray) {
    fun onSolve() {

    }

    fun onDiscard() {

    }
}
