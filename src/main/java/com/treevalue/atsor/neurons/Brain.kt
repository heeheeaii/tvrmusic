package com.treevalue.atsor.neurons

import org.nd4j.linalg.cpu.nativecpu.bindings.Nd4jCpu.NDArray

class Brain {
    lateinit var lay1: Layer
    lateinit var lay2: Layer
    lateinit var lay3: Layer
    lateinit var lay4: Layer
    lateinit var lay5: Layer
    lateinit var lay6: Layer

    val columns = HashMap<IntArray, Colum>()
    fun publish(msg: NDArray, to: IntArray) {}

    fun save(path: String) {}
    fun load(path: String) {}
}
