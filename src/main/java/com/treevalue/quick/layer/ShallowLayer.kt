package com.treevalue.quick.layer

import com.treevalue.quick.SignalTransmissionManager
import com.treevalue.quick.Layer

class ShallowLayer(
    private val signalTransmissionManager: SignalTransmissionManager
) : Layer(signalTransmissionManager = signalTransmissionManager) {

}
