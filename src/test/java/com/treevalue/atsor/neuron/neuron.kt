import kotlin.random.Random
import kotlin.math.exp

data class Tensor(val data: FloatArray, val shape: List<Int>) {

    val size: Int = shape.reduce { acc, i -> acc * i }

    init {
        require(data.size == size) {
            "Data size (${data.size}) must match shape size ($size)"
        }
    }

    operator fun get(row: Int, col: Int): Float {
        require(shape.size == 2) { "Get(row, col) only supported for 2D tensors" }
        require(row >= 0 && row < shape[0]) { "Row index out of bounds: $row" }
        require(col >= 0 && col < shape[1]) { "Column index out of bounds: $col" }
        val index = row * shape[1] + col
        return data[index]
    }

    operator fun set(row: Int, col: Int, value: Float) {
        require(shape.size == 2) { "Set(row, col, value) only supported for 2D tensors" }
        require(row >= 0 && row < shape[0]) { "Row index out of bounds: $row" }
        require(col >= 0 && col < shape[1]) { "Column index out of bounds: $col" }
        val index = row * shape[1] + col
        data[index] = value
    }

    operator fun get(index: Int): Float {
        require(index in 0..<size) { "Flat index out of bounds: $index" }
        return data[index]
    }


    operator fun set(index: Int, value: Float) {
        require(index in 0..<size) { "Flat index out of bounds: $index" }
        data[index] = value
    }


    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Tensor(shape=$shape, data=\n")
        if (shape.size == 2) {
            for (r in 0 until shape[0]) {
                sb.append("  [")
                for (c in 0 until shape[1]) {
                    sb.append(String.format("%.4f", data[r * shape[1] + c]))
                    if (c < shape[1] - 1) sb.append(", ")
                }
                sb.append("]\n")
            }
        } else {

            sb.append("  [")
            sb.append(data.joinToString(", ") { String.format("%.4f", it) })
            sb.append("]\n")
        }
        sb.append(")")
        return sb.toString()
    }

    companion object {
        fun random(shape: List<Int>): Tensor {
            val size = shape.reduceOrNull { acc, i -> acc * i } ?: 0
            val data =
                FloatArray(size) { Random.nextFloat() }
            return Tensor(data, shape)
        }

        fun zeros(shape: List<Int>): Tensor {
            val size = shape.reduceOrNull { acc, i -> acc * i } ?: 0
            val data = FloatArray(size) { 0.0f }
            return Tensor(data, shape)
        }
    }
}

open class Neuron(
    val inputShape: List<Int> = listOf(26, 1),
    val outputShape: List<Int> = listOf(26, 1),
) {
    protected fun doInput(inputTensor: Tensor): Tensor {
        require(inputTensor.shape == this.inputShape) {
            "Input tensor shape ${inputTensor.shape} does not match neuron's expected input shape ${this.inputShape}"
        }
        val outputData = FloatArray(this.outputShape.reduce { acc, i -> acc * i }) { 0f }
        return Tensor(outputData, this.outputShape)
    }


    protected fun doProcess(inputTensor: Tensor): Tensor {
        val outputData = FloatArray(this.outputShape.reduce { acc, i -> acc * i }) { 0f }
        return Tensor(outputData, this.outputShape)
    }

    protected fun doOut(inputTensor: Tensor): Tensor {
        val outputData = FloatArray(this.outputShape.reduce { acc, i -> acc * i }) { 0f }
        return Tensor(outputData, this.outputShape)
    }

    fun processFlow(inputTensor: Tensor): Tensor {
        var outputTensor = doInput(inputTensor)
        outputTensor = doProcess(inputTensor)
        outputTensor = doOut(outputTensor)
        return outputTensor
    }
}


fun main() {


    val neuron = Neuron()
}
