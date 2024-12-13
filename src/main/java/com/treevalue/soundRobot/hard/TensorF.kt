package com.treevalue.soundRobot.hard

import ai.djl.ndarray.NDArray
import com.treevalue.soundRobot.data.Tensor

class TensorF : Tensor<Float> {
    constructor(ndArray: NDArray) : super(ndArray)
}
