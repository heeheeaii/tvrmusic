import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex

fun main() {
    // 创建一个 NDManager
    val manager: NDManager = NDManager.newBaseManager()

    // 创建一个输入张量（例如，随机生成一个形状为 (1, 5, 5, 3) 的张量）
    val input: NDArray = manager.randomUniform(0f, 1f, longArrayOf(1, 5, 5, 3)) // (batch_size, height, width, channels)

    // 创建卷积核 (3x3)
    val kernel: NDArray = manager.zeros(longArrayOf(3, 3, 3, 1))  // (height, width, input_channels, output_channels)

    // 自定义卷积操作
    val output = convolution2D(input, kernel)

    // 打印输出
    println(output)
}

// 定义卷积操作
fun convolution2D(input: NDArray, kernel: NDArray): NDArray {
    val inputShape = input.shape
    val kernelShape = kernel.shape

    val outputHeight = inputShape[1] - kernelShape[0] + 1
    val outputWidth = inputShape[2] - kernelShape[1] + 1

    // 输出张量
    val output = input.get(NDIndex.all(), NDIndex.interval(0, outputHeight), NDIndex.interval(0, outputWidth))

    // 使用卷积核对每个像素进行计算
    for (i in 0 until outputHeight.toInt()) {
        for (j in 0 until outputWidth.toInt()) {
            val subArray = input.get(NDIndex.all(), NDIndex.interval(i, i + kernelShape[0]), NDIndex.interval(j, j + kernelShape[1]))
            val result = subArray.mul(kernel).sum()
            output.set(result, NDIndex.all(), NDIndex.interval(i, i + 1), NDIndex.interval(j, j + 1))
        }
    }

    return output
}
