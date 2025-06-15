package com.treevalue.quick.layer

import com.treevalue.quick.SignalTransmissionManager
import com.treevalue.quick.Layer

class FeedbackLayer(
    private val signalTransmissionManager: SignalTransmissionManager
) : Layer(signalTransmissionManager = signalTransmissionManager) {
}
