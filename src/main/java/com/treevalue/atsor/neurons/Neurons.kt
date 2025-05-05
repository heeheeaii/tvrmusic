package com.treevalue.atsor.neurons

import ai.djl.ndarray.NDArray
import java.io.Serializable

open class Neurons : Serializable {
    lateinit var coordinate: IntArray
    fun input(inData: NDArray) {} // only complex neurotransmitters

    // [excitatory, inhibitory
    // [glutamate(兴奋), gaba(抑制),histamine(兴奋偏置), dopamine(注意力),
    // acetylcholine(记忆), norepinephrine(强化)
    fun output(outData: NDArray) {}
    // [id1, medium11,12..., id2, med21, 22...
    // [id , excitatory, inhibitory
    /*
    has 26 nearby neurons,
     */
}
