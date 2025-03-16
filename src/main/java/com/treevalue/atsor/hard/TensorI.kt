package com.treevalue.atsor.hard

import ai.djl.ndarray.NDArray
import com.treevalue.atsor.data.Tensor

class TensorI : Tensor<Int> {
    constructor(ndArray: NDArray) : super(ndArray)
    constructor(size: Int, default: Int) : super(size, default)
}
