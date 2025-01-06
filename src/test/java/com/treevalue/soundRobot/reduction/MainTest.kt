import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape

fun prettyPrint(array: NDArray, indent: String = "") {
    val shape = array.shape
    val data = array.toFloatArray()

    fun recursivePrint(data: FloatArray, shape: Shape, level: Int, currentIndent: String) {
        if (shape.dimension() == 0) {
            println(currentIndent + data[0])
            return
        }

        val size = shape[0].toInt()
        val remainingShape = if (shape.dimension() > 1) shape.slice(1, shape.dimension()) else Shape()

        println(currentIndent + "[")
        for (i in 0 until size) {
            val sliceSize = if (remainingShape.dimension() > 0) remainingShape.size().toInt() else 1
            val start = i * sliceSize
            val end = start + sliceSize

            if (remainingShape.dimension() > 0) {
                val slice = data.sliceArray(start until end)
                recursivePrint(slice, remainingShape, level + 1, currentIndent + "  ")
            } else {
                print(currentIndent + "  " + data[start])
            }

            if (i < size - 1) {
                println(",")
            }
        }
        println()
        print(currentIndent + "]")
    }

    recursivePrint(data, shape, 0, indent)
    println()
}

fun removeSmoothRegions(manager: NDManager, tensor: NDArray, threshold: Float): NDArray {
    val gradientX = tensor.get("1:, :").sub(tensor.get(":-1, :"))
    val gradientY = tensor.get(":, 1:").sub(tensor.get(":, :-1"))

    val paddedGradientX = manager.zeros(tensor.shape)
    paddedGradientX.set(NDIndex("1:"), gradientX)

    val paddedGradientY = manager.zeros(tensor.shape)
    paddedGradientY.set(NDIndex(":, 1:"), gradientY)

    val gradientMagnitude = paddedGradientX.pow(2).add(paddedGradientY.pow(2)).pow(0.5)

    val smoothMask = gradientMagnitude.lt(threshold)

    val originalTensor = tensor.duplicate()

    val modifiedTensor = tensor.toType(DataType.FLOAT32, false)
    modifiedTensor.set(smoothMask, Float.NaN)
    return modifiedTensor
}

fun main() {
    val manager = NDManager.newBaseManager()
    val tensor = manager.randomUniform(0f, 1f, Shape(3,3))
    val threshold = 0.1f

    val modifiedTensor = removeSmoothRegions(manager, tensor, threshold)

    prettyPrint(modifiedTensor)
}
