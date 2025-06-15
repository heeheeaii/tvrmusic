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
            val feeling: INDArray = Nd4j.create(floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 1f, 0f), intArrayOf(10))
            val except: INDArray = Nd4j.create(floatArrayOf(0f, 1f, 0f, 0f, 1f), intArrayOf(5))
            for (idx in 0..5) {
                transform.except(feeling, except)
                val (eventId: UUID, pre1: INDArray) = transform.predicate(feeling)
                val isOk = Monitor.areTensorsEqual(feeling, except)
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
