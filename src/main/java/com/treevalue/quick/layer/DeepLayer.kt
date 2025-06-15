package com.treevalue.quick.layer

import com.treevalue.quick.SignalTransmissionManager
import com.treevalue.quick.Layer
import org.nd4j.linalg.api.ndarray.INDArray

class DeepLayer(
    private val signalTransmissionManager: SignalTransmissionManager
) : Layer(signalTransmissionManager = signalTransmissionManager) {
    fun input(feeling: INDArray) {
    }
}
