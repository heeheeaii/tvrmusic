package com.treevalue.quick.layer

import com.treevalue.quick.EventHandler
import com.treevalue.quick.Layer
import org.nd4j.linalg.api.ndarray.INDArray

class OutputLayer(
    private val eventHandler: EventHandler
) : Layer(eventHandler = eventHandler) {

}
