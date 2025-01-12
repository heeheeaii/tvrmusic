import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import ai.djl.util.Utils

fun normalizeTensor(tensor: NDArray, max: Double = 1.0): NDArray {
    val min = tensor.min().getFloat()
    val maxVal = tensor.max().getFloat()

    return if (min == maxVal) {
        if (min == 0.0f) tensor.zerosLike() else tensor.onesLike().mul(max)
    } else {
        val normalizedTensor = tensor.sub(min).div(maxVal - min)
        normalizedTensor.toType(DataType.FLOAT64, true).mul(max)
    }
}

fun extractShapeBoundaries(tensor2D: NDArray): NDArray {
    val rows = tensor2D.shape[0].toInt()
    val cols = tensor2D.shape[1].toInt()

    val boundaries = NDManager.newBaseManager().create(Shape(rows.toLong(), cols.toLong()), DataType.UINT8)

    for (i in 1 until rows - 1) {
        for (j in 1 until cols - 1) {
            if (tensor2D.getFloat(i.toLong(), j.toLong()) > 0.5f) {
                val neighbors = listOf(
                    tensor2D.getFloat((i - 1).toLong(), j.toLong()), // 上
                    tensor2D.getFloat((i + 1).toLong(), j.toLong()), // 下
                    tensor2D.getFloat(i.toLong(), (j - 1).toLong()), // 左
                    tensor2D.getFloat(i.toLong(), (j + 1).toLong())  // 右
                )
                if (neighbors.any { it <= 0.5f }) {
                    boundaries.set(NDIndex(i.toLong(), j.toLong()), 1f)
                }
            }
        }
    }
    return boundaries
}

fun extractLineSegments(tensor1D: NDArray): NDArray {
    val lineSegments = NDManager.newBaseManager().create(tensor1D.shape)
    var start = -1

    val nonZeroIndices = tensor1D.toType(DataType.INT32, true).gt(0.5f).toIntArray()
        .mapIndexed { index, value -> if (value == 1) index else null }

    nonZeroIndices.forEachIndexed { i, idx ->
        if (start == -1) {
            start = idx!!
        }
        if (i == nonZeroIndices.size - 1 || nonZeroIndices[i]?.plus(1) != nonZeroIndices[i + 1]) {
            for (j in start..idx!!) {
                lineSegments.set(NDIndex(j.toLong()), tensor1D.get(j.toLong()))
            }
            start = -1
        }
    }

    return lineSegments
}

fun main() {
    val manager = NDManager.newBaseManager()

    val tensor2D = manager.create(
        arrayOf(
            floatArrayOf(0f, 0f, 255f, 255f, 0f),
            floatArrayOf(0f, 255f, 0f, 0f, 255f),
            floatArrayOf(255f, 0f, 0f, 0f, 255f),
            floatArrayOf(0f, 255f, 0f, 0f, 255f),
            floatArrayOf(0f, 0f, 255f, 255f, 0f)
        )
    )

    val tensor1D = manager.create(floatArrayOf(0f, 1f, 1f, 0f, 0f, 2f, 1f, 2f, 0f, 0f, 1f, 1f, 0f))

    val shapeBoundaries = extractShapeBoundaries(tensor2D)
    val normalizedLineSegments = normalizeTensor(tensor1D)
    val lineSegments = extractLineSegments(normalizedLineSegments)

    println("Shape Boundaries:\n${shapeBoundaries}")
    println("Line Endpoints:\n${lineSegments}")
}
