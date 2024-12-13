package com.treevalue.soundRobot.data

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.types.DataType

open class Tensor<T> : Iterator<T> {
    var shape: List<Int>
        private set
    private lateinit var data: MutableList<T>
    var size = 0
    private var itorIdx = 0

    constructor(size: Int, default: T) {
        this.shape = mutableListOf(size)
        this.data = MutableList(size) { default }
    }

    constructor(ndArray: NDArray) {
        shape = ndArray.shape.shape.toMutableList().map { it.toInt() }
        data = ndArray.toFloatArray().toMutableList() as MutableList<T>
    }

    constructor(shape: List<Int>, data: MutableList<T>) {
        size = shape.reduce { acc, it -> acc * it }
        require(size == data.size) { "data number size error" }
        this.shape = shape
        this.data = data

    }

    fun update(idxBeg: IntArray, idxEnd: IntArray, inData: List<T>) {
        require(idxBeg.size == shape.size && idxEnd.size == shape.size) { "Indices must match the tensor's shape" }

        val begIndex = calculateIndex(idxBeg)
        val endIndex = calculateIndex(idxEnd)

        require(begIndex <= endIndex && endIndex < this.data.size) { "Invalid index range" }
        require(endIndex - begIndex + 1 == inData.size) { "Data size does not match index range" }

        for (i in 0 until inData.size) {
            this.data[begIndex + i] = inData[i]
        }
    }

    constructor(shape: List<Int>, default: T) {
        size = shape.reduce { acc, it -> acc * it }
        require(size == data.size) { "data number size error" }
        this.shape = shape
        this.data = MutableList(size) { default }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Tensor<*>) return false
        if (this === other) return true
        var idx = 0;
        for (itm in other) {
            if (idx < data.size) {
                if (itm != data[idx++]) return false
            }
        }
        return true
    }

    override fun hasNext(): Boolean {
        return itorIdx < data.size
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        return data[itorIdx++]
    }

    operator fun get(vararg indices: Int): T {
        require(indices.size == shape.size) { "Number of indices must match the tensor's shape" }
        val index = calculateIndex(indices)
        return data[index]
    }

    operator fun set(vararg indices: Int, value: T) {
        require(indices.size == shape.size) { "Number of indices must match the tensor's shape" }
        val index = calculateIndex(indices)
        data[index] = value
    }

    private fun calculateIndex(indices: IntArray): Int {
        var index = 0
        var multiplier = 1
        for (i in indices.indices.reversed()) {
            index += indices[i] * multiplier
            multiplier *= shape[i]
        }
        return index
    }

    override fun toString(): String {
        return buildString {
            append("Tensor(shape=$shape, data=$data)")
        }
    }

    private fun <T> checkToPrintStringRoStringBuilder(
        index: Int,
        stringBuilder: StringBuilder,
        dataSource: MutableList<T>,
        lens: MutableList<Int>,
        begPosition: Int
    ): Boolean {
        if (index < lens.size) {
            var all = 1
            lens.forEach {
                if (it <= 0) return false
                all *= it
            }
            if (dataSource.size == all) return true
        }
        return false
    }

    private fun <T> toPrintStringRoStringBuilder(
        index: Int,
        stringBuilder: StringBuilder,
        dataSource: MutableList<T>,
        lens: MutableList<Int>,
        begPosition: Int
    ) {
        if (checkToPrintStringRoStringBuilder(index, stringBuilder, dataSource, lens, begPosition)) {
            stringBuilder.append("[")
            for (idx in 0 until lens[index]) {
                if (index < lens.size - 1) {
                    var step = 1
                    for (i in index + 1 until lens.size) {
                        step *= lens[i]
                    }
                    toPrintStringRoStringBuilder(index + 1, stringBuilder, dataSource, lens, begPosition + idx * step)
                    if (idx != lens[index] - 1) stringBuilder.append(", ")
                } else {
                    stringBuilder.append(dataSource[begPosition + idx])
                    if (idx != lens[index] - 1) stringBuilder.append(", ")
                }
            }
            stringBuilder.append("]")
            return
        }
        throw IllegalArgumentException()
    }


    fun toPrintString(): String {
        TODO()
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }

    companion object {
        fun <T> create(shape: List<Int>, initializer: (IntArray) -> T): Tensor<T> {
            val size = shape.reduce { acc, i -> acc * i }
            val data = MutableList<T>(size) { initializer(IntArray(shape.size)) }
            val tensor = Tensor(shape, data)
            fillTensor(tensor, shape, initializer)
            return tensor
        }

        private fun <T> fillTensor(tensor: Tensor<T>, shape: List<Int>, initializer: (IntArray) -> T) {
            val totalElements = shape.reduce { acc, i -> acc * i }
            val indices = IntArray(shape.size)

            for (i in 0 until totalElements) {
                var temp = i
                for (j in shape.indices.reversed()) {
                    indices[j] = temp % shape[j]
                    temp /= shape[j]
                }
                tensor.set(*indices, value = initializer(indices))
            }

        }

    }
}
