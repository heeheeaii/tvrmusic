import ai.djl.modality.cv.ImageFactory
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries
import org.knowm.xchart.style.markers.SeriesMarkers
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.geom.AffineTransform
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ImageUtil {
    //    more strength than 2
    private val sharpenKernel1 = floatArrayOf(
        -1f, 0f, -1f,
        0f, 5f, 0f,
        -1f, 0f, -1f
    )

    private val sharpenKernel2 = floatArrayOf(
        0f, -1f, 0f,
        -1f, 5f, -1f,
        0f, -1f, 0f
    )

    fun convolve(
        inputPath: String,
        outPath: String,
        convKernel: FloatArray = floatArrayOf(),
        width: Int = 3,
        height: Int = 3,
        type: String = "png"
    ) {
        val inputImage: BufferedImage = ImageIO.read(File(inputPath))
        val kernel = if (convKernel.isNotEmpty()) Kernel(width, height, convKernel)
        else Kernel(width, height, sharpenKernel1)
        val convolveOp = ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null)
        val outputImage: BufferedImage = convolveOp.filter(inputImage, null)
        ImageIO.write(outputImage, type, File(outPath))
    }

    fun drawScatterPlot(
        coordinates: List<Pair<Double, Double>>,
        outputPath: String,
        multi: Int = 800
    ) {
        val xData = coordinates.map { it.first }
        val yData = coordinates.map { it.second }
        val width: Int = (xData.size / 300 + 1) * multi
        val height: Int = width * 3 / 4
        val chart = XYChartBuilder()
            .width(width)
            .height(height)
            .title("Scatter Plot")
            .xAxisTitle("X Axis")
            .yAxisTitle("Y Axis")
            .build()
        val series = chart.addSeries("_", xData, yData)
        series.marker = SeriesMarkers.CIRCLE
        chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter

        File(outputPath).outputStream().use {
            org.knowm.xchart.BitmapEncoder.saveBitmap(chart, it, org.knowm.xchart.BitmapEncoder.BitmapFormat.PNG)
        }
    }

    fun loadTensorFromBinary(manager: NDManager, filePath: String, compress: Boolean = true): NDArray {
        val byteList = mutableListOf<Byte>()
        FileInputStream(filePath).use { fis ->
            val inStream = if (compress) GZIPInputStream(fis) else fis
            BufferedInputStream(inStream).use { bis ->
                var b = bis.read()
                while (b != -1) {
                    byteList.add(b.toByte())
                    b = bis.read()
                }
            }
        }
        val bytes = byteList.toByteArray()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

        val dataTypeOrdinal = buffer.int
        val dataType = DataType.values()[dataTypeOrdinal]

        val shapeSize = buffer.int
        val shape = LongArray(shapeSize) { buffer.long }.toMutableList()

        val numElements = shape.reduce { acc, i -> acc * i }
        val bytesPerElement = dataType.numOfBytes
        val expectedBytes = numElements * bytesPerElement

        if (buffer.remaining() != expectedBytes.toInt()) {
            throw IOException("Incorrect number of bytes in the binary file. Expected: $expectedBytes, Actual: ${buffer.remaining()}")
        }

        val dataBytes = ByteArray(buffer.remaining())
        buffer.get(dataBytes)  // Now this should read the correct amount of data
        val dataBuffer =
            ByteBuffer.wrap(dataBytes).order(ByteOrder.BIG_ENDIAN) // Ensure correct byte order for data as well
        return manager.create(dataBuffer, Shape(shape), dataType)
    }

    fun saveTensorToBinary(tensor: NDArray, filePath: String, compress: Boolean = true) {
        val dataType = tensor.dataType
        val shape = tensor.shape.shape
        val byteArray = tensor.toByteArray()

        FileOutputStream(filePath).use { fos ->
            val outStream = if (compress) GZIPOutputStream(fos) else fos
            BufferedOutputStream(outStream).use { bos ->
                bos.write(dataType.ordinal) // Write data type ordinal
                bos.write(shape.size)       // Write shape size


                shape.forEach { dim ->
                    bos.write(ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(dim).array())
                }
                bos.write(byteArray)        // Write the actual tensor data
            }
        }
    }

    fun rgbConvertToGrayscaleOneChanel(rgbTensor: NDArray, manager: NDManager, outPath: String) {

        val tensor2 = manager.full(Shape(rgbTensor.shape[0], rgbTensor.shape[1], 1), 0F)
        val shape = rgbTensor.shape.shape
        for (idx in 0 until shape[0]) {
            for (jdx in 0 until shape[1]) {
                val r = rgbTensor.getUint8(idx, jdx, 0).toFloat()
                val g = rgbTensor.getUint8(idx, jdx, 1).toFloat()
                val b = rgbTensor.getUint8(idx, jdx, 2).toFloat()
                val level: Float = ImageUtil.getGrayLevel(r, g, b)
                tensor2.set(NDIndex("${idx},${jdx},0"), level)
            }
        }
        saveTensorAsGrayImage(tensor2, outPath)
    }

    fun saveTensorAsGrayImage(tensor: NDArray, filePath: String, type: String = "png") {
        try {
            val shape = tensor.shape.shape
            val channels: Int
            val width: Int
            val height: Int
            when (shape.size) {
                2 -> {
                    channels = 1
                    height = shape[0].toInt()
                    width = shape[1].toInt()
                }

                3 -> {
                    channels = shape[2].toInt()
                    height = shape[0].toInt()
                    width = shape[1].toInt()
                    if (channels !in listOf(1, 3, 4)) {
                        throw IllegalArgumentException("Unsupported number of channels: $channels. Supported channels are 1, 3, or 4.")
                    }
                }

                else -> {
                    throw IllegalArgumentException("Tensor must be 2D (grayscale) or 3D with channels. Current shape size is ${shape.size}")
                }
            }
            val data: FloatArray = when (tensor.dataType) {
                DataType.FLOAT16, DataType.FLOAT32, DataType.FLOAT64 -> tensor.toFloatArray()
                else -> tensor.toArray().map { it.toFloat() }.toFloatArray()
            }

            val imageType = when (channels) {
                1 -> BufferedImage.TYPE_BYTE_GRAY
                3 -> BufferedImage.TYPE_3BYTE_BGR
                4 -> BufferedImage.TYPE_4BYTE_ABGR
                else -> throw IllegalArgumentException("Unsupported number of channels: $channels")
            }

            val image = BufferedImage(width, height, imageType)
            val raster = image.raster
            raster.setPixels(0, 0, width, height, data)
            ImageIO.write(image, type, File(filePath))
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            println("Argument Error: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error saving image: ${e.message}")
        }
    }

    fun rgbConvertToGrayscaleThreeChanel(inputImagePath: String, outputImagePath: String) {
        try {
            val originalImage = ImageIO.read(File(inputImagePath))
            val width = originalImage.width
            val height = originalImage.height

            val grayscaleImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgb = originalImage.getRGB(x, y)
                    val color = Color(rgb)
                    val gray = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue).toInt()
                    val newRGB = Color(gray, gray, gray).rgb
                    grayscaleImage.setRGB(x, y, newRGB)
                }
            }

            ImageIO.write(grayscaleImage, "jpg", File(outputImagePath))
            println("Converted to grayscale: $outputImagePath")

        } catch (e: Exception) {
            println("Error converting image: ${e.message}")
        }
    }

    fun rotateImage(inputImagePath: String, outputImagePath: String?, angleDegrees: Double) {
        try {
            val inputImage = ImageIO.read(File(inputImagePath))
                ?: throw IOException("Could not read image file: $inputImagePath")
            val width = inputImage.width
            val height = inputImage.height
            val angleRadians = Math.toRadians(angleDegrees)
            val rotatedWidth = abs(width * cos(angleRadians)) + abs(height * sin(angleRadians))
            val rotatedHeight = abs(width * sin(angleRadians)) + abs(height * cos(angleRadians))
            val rotatedImage = BufferedImage(rotatedWidth.toInt(), rotatedHeight.toInt(), inputImage.type)
            val transform = AffineTransform()
            transform.rotate(angleRadians, rotatedWidth / 2, rotatedHeight / 2)
            val g2d = rotatedImage.createGraphics()
            g2d.drawImage(inputImage, transform, null)
            g2d.dispose()
            val format = inputImagePath.substringAfterLast(".")
            if (!ImageIO.write(rotatedImage, format, File(outputImagePath))) {
                throw IOException("Could not write image in format $format")
            }
        } catch (e: IOException) {
            System.err.println("Error rotating image: ${e.message}")
        } catch (e: Exception) {
            System.err.println("An unexpected error occurred: ${e.message}")
        }
    }

    fun imagTensorSave(ndArray: NDArray, path: String): Boolean {
        try {
            FileOutputStream(path).use { fos ->
                DataOutputStream(fos).use { dos ->
                    val shape = ndArray.shape.shape
                    dos.writeInt(shape.size)
                    for (dim in shape) {
                        dos.writeLong(dim)
                    }

                    val dataType = ndArray.dataType
                    dos.writeUTF(dataType.name)


                    val buffer = ndArray.toByteBuffer()
                    dos.writeInt(buffer.capacity())
                    dos.write(buffer.array())

                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getGrayLevel(r: Float, g: Float, b: Float): Float {
        return (0.299 * r + 0.587 * g + 0.114 * b).toFloat()
    }

    fun loadImaAsTensor(path: String, manager: NDManager?): NDArray {
        val img = ImageFactory.getInstance().fromFile(Path.of(path))
        return img.toNDArray(manager)
    }
}

