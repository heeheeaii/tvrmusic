package com.treevalue.soundRobot.data

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.types.Shape
import com.treevalue.soundRobot.data.simple.FixedSizeQueue

class TensorContainer(
    private val capacity: Int,
    private val sizePerTensor: Array<Int> = arrayOf<Int>(capacity),
    private val shapes: Array<Shape?> = arrayOfNulls<Shape>(capacity)
) {
    private val tensors = arrayOfNulls<FixedSizeQueue<NDArray>>(capacity)

    init {
        require(sizePerTensor.size == capacity) { "size not right" }
        for (idx in 0 until capacity) {
            tensors[idx] = FixedSizeQueue<NDArray>(sizePerTensor[idx])
        }
    }

    operator fun set(idx: Int, tensor: NDArray) {
        addTensor(idx, tensor)
    }

    private fun addTensor(idx: Int, tensor: NDArray) {
        require(idx in 0 until capacity) { "Index out of bounds" }
        if (tensors[idx]?.size() == 0) {
            shapes[idx] = tensor.shape
            tensors[idx]?.enqueue(tensor)
        } else {
            require(tensor.shape == shapes[idx]) { "Tensor shape mismatch" }
            tensors[idx]?.enqueue(tensor)
        }
    }

    operator fun get(idx: Int): NDArray? {
        return getTensor(idx)
    }

    private fun getTensor(idx: Int): NDArray? {
        require(idx in 0 until capacity) { "Index out of bounds" }
        return tensors[idx]?.peek()
    }

    fun getCapacity(): Int {
        return capacity
    }
}
