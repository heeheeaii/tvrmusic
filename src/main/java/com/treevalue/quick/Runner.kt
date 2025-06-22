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
            var predicated: INDArray? = null
            for (idx in 0..3) {
                val predRst = transform.predicate(feeling)
                val eventId: UUID = predRst.first
                predicated = predRst.second
                val isOk: Boolean = Monitor.arePosEqual(feeling, except)
                if (!isOk) {
                    transform.reinforce(except, predicated, eventId)
                    transform.except(feeling, except)
                } else {
                    break
                }
            }
            predicated?.let {
                val exceptTensor: INDArray = transform.transPositionToTensor(except, transform.outputShape())
                println(Monitor.areTensorsEqual(exceptTensor, it))
            }
        }
    }
}
