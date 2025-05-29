package com.treevalue.quick

import AStarPathfinder
import com.treevalue.atsor.hard.TensorManager
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class Runner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val transform = Transform.getInstance()
            val feeling:INDArray = Nd4j.create(10)
            transform.input(feeling)
            AStarPathfinder().matchPointsByMinCost()
//            transform.active()
//            GrowthManager.getInstance().requestGrowth()
        }
    }
}
