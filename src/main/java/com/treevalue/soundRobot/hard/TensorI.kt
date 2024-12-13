package com.treevalue.soundRobot.hard

import ai.djl.ndarray.NDArray
import com.treevalue.soundRobot.data.Tensor

class TensorI : Tensor<Int> {
    constructor(ndArray: NDArray) : super(ndArray)
    constructor(size: Int, default: Int) : super(size, default)
}
