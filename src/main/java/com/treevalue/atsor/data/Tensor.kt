package com.treevalue.atsor.data

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape

class Tensor(val data: FloatArray, val shape: LongArray) {

    companion object {
        fun toNDArray(manager: NDManager, tensor: Tensor): NDArray {
            return manager.create(tensor.data, Shape(*tensor.shape))
        }

        fun fromNDArray(ndArray: NDArray): Tensor {
            val floatData = ndArray.toFloatArray()
            val longShape = ndArray.shape.shape
            return Tensor(floatData, longShape)
        }
    }
}
