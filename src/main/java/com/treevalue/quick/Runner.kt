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
            for (idx in 0..5) {
                transform.except(feeling, except)
                val (eventId: UUID, pre1: INDArray) = transform.predicate(feeling)
                val isOk = Monitor.areTensorsEqual(feeling.toTypedArray(), except.toTypedArray())
                if (!isOk) {
                    Monitor.reinforce(except, pre1, eventId)
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
