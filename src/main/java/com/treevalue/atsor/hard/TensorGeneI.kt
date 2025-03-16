package com.treevalue.atsor.hard

import ai.djl.ndarray.NDArray
import com.treevalue.atsor.data.TensorGene

class TensorGeneI : TensorGene<Int> {
    constructor(ndArray: NDArray) : super(ndArray)
    constructor(size: Int, default: Int) : super(size, default)
}
