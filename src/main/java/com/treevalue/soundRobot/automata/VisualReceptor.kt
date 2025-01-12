import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class VisualReceptor(private var manager: NDManager? = null, private var executor: ScheduledExecutorService? = null) :
    Closeable {
    private val channels = 3
    private val robot: Robot = Robot()
    private val screenRect: Rectangle = Rectangle(Toolkit.getDefaultToolkit().screenSize)

    @Volatile
    private var screenTensor: NDArray? = null

    init {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor { runnable ->
                Thread(runnable).apply { isDaemon = true }
            }
        }
        if (manager == null) {
            manager = NDManager.newBaseManager()
        }
        startCapture()
    }

    private fun startCapture() {
        executor!!.scheduleAtFixedRate({
            try {
                screenTensor = imageToNDArray(robot.createScreenCapture(screenRect))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, 50, TimeUnit.MILLISECONDS) // 20 Hz = 50 ms interval
    }

    private fun imageToNDArray(image: BufferedImage): NDArray {
        val width = image.width
        val height = image.height
        val data = IntArray(width * height * channels)
        image.getRGB(0, 0, width, height, data, 0, width)

        val floatData = FloatArray(data.size)
        for (i in 0 until data.size / 3) {
            val argb = data[i]
            val r = (argb shr 16 and 0xFF).toFloat()
            val g = (argb shr 8 and 0xFF).toFloat()
            val b = (argb and 0xFF).toFloat()
            floatData[i * 3] = r
            floatData[i * 3 + 1] = g
            floatData[i * 3 + 2] = b
        }

        return manager!!.create(floatData, Shape(height.toLong(), width.toLong(), channels.toLong()))
    }

    fun getScreenTensor(): NDArray? {
        return screenTensor
    }

    override fun close() {
        executor!!.shutdown()
    }
}
