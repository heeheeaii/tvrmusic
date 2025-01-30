package com.treevalue.soundRobot.search

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import com.treevalue.soundRobot.hard.TensorManager

class VideoSequenceGenerator(val videoPath: String, val manager: NDManager = TensorManager.getManager()) {
    fun getSequence(from: Int, to: Int): List<NDArray>? {
        return null
    }
}
