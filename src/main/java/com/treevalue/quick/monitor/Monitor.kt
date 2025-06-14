package com.treevalue.quick.monitor

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.ops.transforms.Transforms

class Monitor {
    fun areTensorsEqual(tensor1: INDArray, tensor2: INDArray, threshold: Float = 0.001f): Boolean {
        if (tensor1.isEmpty || tensor2.isEmpty) {
            return false
        }
        if (!tensor1.equalShapes(tensor2)) {
            return false
        }
        val difference = tensor1.sub(tensor2)
        val maxAbsValueTensor1 = Transforms.abs(tensor1).maxNumber().toFloat()
        val maxAbsValueTensor2 = Transforms.abs(tensor2).maxNumber().toFloat()
        if (maxAbsValueTensor1 <= threshold && maxAbsValueTensor2 <= threshold) {
            val maxAbsDiff = Transforms.abs(difference).maxNumber().toFloat()
            return maxAbsDiff < threshold
        }
        val norm = difference.norm2Number().toFloat()
        return norm < threshold
    }
}
