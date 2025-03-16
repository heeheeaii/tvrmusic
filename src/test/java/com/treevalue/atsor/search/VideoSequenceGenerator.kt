package com.treevalue.atsor.search

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import com.treevalue.atsor.hard.TensorManager

class VideoSequenceGenerator(val videoPath: String, val manager: NDManager = TensorManager.getManager()) {
    fun getSequence(from: Int, to: Int): List<NDArray>? {
        return null
    }
}
