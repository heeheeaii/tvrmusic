package com.treevalue.soundRobot.search

import ai.djl.ndarray.NDArray

class Searcher {
    private var models: MutableList<MutableList<PointCloud>> = ArrayList<MutableList<PointCloud>>()
    fun put(tensors: List<NDArray>) {
    }

    fun search(tensor: NDArray): PointCloud {
        TODO()
    }

    fun sample(tensor: NDArray): PointCloud {
        TODO()
    }
}
