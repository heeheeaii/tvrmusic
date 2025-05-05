package com.treevalue.atsor.neurons

import org.nd4j.linalg.cpu.nativecpu.bindings.Nd4jCpu.NDArray

open class Layer {
    //    now: 1 msg route
    val layerNumber = NeuronsCfg. LAYER_NUMBER_IN_LAYER
    val neurons: List<List<Neurons>> = ArrayList<ArrayList<Neurons>>()
    fun initialize() {}
    fun recMsg(to: IntArray, msg: NDArray) {}
    fun pugMsg(to: IntArray, msg: NDArray) {}
}
