package com.treevalue.soundRobot.search

import ai.djl.ndarray.NDArray

class Searcher {
//     3 main trouble
//     1 sequence tensor model extract
//     2 model dynamic change
//     3 model match
    private var models: MutableList<MutableList<PointCloud>> = ArrayList<MutableList<PointCloud>>()
    fun put(tensors: List<NDArray>) {
//        Reduction().get()
    }

    fun search(tensor: NDArray): PointCloud {
        TODO()
    }

    fun sample(tensor: NDArray): PointCloud {
        TODO()
    }
}
