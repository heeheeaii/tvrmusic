package com.treevalue.atsor.hard

import ai.djl.ndarray.NDArray
import com.treevalue.atsor.data.Tensor

class TensorF : Tensor<Float> {
    constructor(ndArray: NDArray) : super(ndArray)
}
