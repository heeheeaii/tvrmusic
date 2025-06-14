package com.treevalue.quick

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class Runner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val transform = Transform.getInstance()
            val feeling: INDArray = Nd4j.create(floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 1f, 0f), intArrayOf(10))
            val except: INDArray = Nd4j.create(floatArrayOf(0f, 1f, 0f, 0f, 1f), intArrayOf(5))
            transform.input(feeling)
            transform.except(except)
            val predicateTensor : Unit = transform.predicate(feeling)
            predicateTensor.equals(except)
//            AStarPathfinder().matchPointsByMinCost()
//            transform.active()
//            GrowthManager.getInstance().requestGrowth()
        }
    }
}
