package com.treevalue.atsor.hard

import ai.djl.ndarray.NDArray
import com.treevalue.atsor.data.TensorGene

class TensorGeneF : TensorGene<Float> {
    constructor(ndArray: NDArray) : super(ndArray)
}
