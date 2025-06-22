package com.treevalue.quick

import com.treevalue.quick.monitor.Monitor
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.util.UUID

class Runner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val transform = Transform.getInstance()

            val feeling = transform.getRandomInput(10)
            val except = transform.getRandomOutput(10)
            for (idx in 0..3) {
                val (eventId: UUID, predicated: INDArray) = transform.predicate(feeling)
                val isOk: Boolean = Monitor.arePosEqual(feeling, except)
                if (!isOk) {
                    transform.reinforce(except, predicated, eventId)
                    transform.except(feeling, except)
                } else {
                    break
                }
            }

//            AStarPathfinder().matchPointsByMinCost()
//            transform.active()
//            GrowthManager.getInstance().requestGrowth()
        }
    }
}
