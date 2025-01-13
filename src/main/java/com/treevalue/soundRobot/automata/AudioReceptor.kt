import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader
import org.jtransforms.fft.DoubleFFT_1D
import java.io.Closeable
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

class AudioReceptor(
    private var manager: NDManager? = null,
    private var executor: ScheduledExecutorService? = null
) : Closeable {

    private val minFreq = 20 // hz
    private val durFreq = 5 // hz
    private val channels = 2 // Stereo
    private val sampleRate = 44100.0f // Standard sample rate
    private val windowSizeMs = 50L // sound duration window
    private val bufferSize = (sampleRate * windowSizeMs / 1000).toInt() // Number of samples for duration window
    private val fft = DoubleFFT_1D(bufferSize.toLong())
    private val frequencyBands = 4406 // (22050 - 20) / 5
    private val tensorShape = Shape(channels.toLong(), frequencyBands.toLong())
    private val sampleBit = 16

    @Volatile
    private var audioTensor: NDArray? = null

    private var audioLine: TargetDataLine? = null
    private val audioBuffer = ByteArray(bufferSize * channels * 2) // 16-bit audio, *2 : 8bit * 2

    // Variables for file reading
    private var audioInputStream: AudioInputStream? = null
    private var fileBuffer: ByteArray? = null
    private var fileDoubleSamples: DoubleArray? = null
    private var fileSampleIndex = 0
    private var fileChannels = 0
    private var fileSampleRate = 0f
    private var fileBytesRead = 0


    init {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor { runnable ->
                Thread(runnable).apply { isDaemon = true }
            }
        }
        if (manager == null) {
            manager = NDManager.newBaseManager()
        }
        setupAudioLine()
        startCapture()
    }

    private fun setupAudioLine() {
        val format = AudioFormat(
            sampleRate,
            sampleBit,
            channels,
            true,
            false
        )
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            throw IllegalArgumentException("Line not supported")
        }
        audioLine = AudioSystem.getLine(info) as TargetDataLine
        audioLine?.open(format, bufferSize * channels * 2)
        audioLine?.start()
    }

    private fun startCapture() {
        executor?.scheduleAtFixedRate({
            try {
                var offset = 0
                while (offset < audioBuffer.size) {
                    val bytesRead = audioLine!!.read(audioBuffer, offset, audioBuffer.size - offset)
                    if (bytesRead <= 0) {
                        break
                    }
                    offset += bytesRead
                }
                if (offset > 0) {
                    val floatSamples = byteArrayToFloat(audioBuffer, channels)
                    audioTensor = processAudioSamples(floatSamples)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, windowSizeMs, TimeUnit.MILLISECONDS)
    }

    private fun processAudioSamples(floatSamples: FloatArray): NDArray? {
        val magnitudes = Array(channels) { DoubleArray(frequencyBands) }
        val perSize = floatSamples.size / channels
        for (ch in 0 until channels) {
            val fftData = DoubleArray(perSize)
            for (i in 0 until perSize) {
                fftData[i] = floatSamples[ch * perSize + i].toDouble()
            }
            fft.realForward(fftData)
            val fftSize = perSize / 2 + 1 // Number of unique frequencies
            for (idx in 0 until frequencyBands) {
                val freq = minFreq + idx * durFreq
                val index = (freq * perSize / sampleRate / 2).toInt() // Correct index calculation
                if (index < fftSize) { // Check if index is within valid range
                    val real = fftData[2 * index]
                    val imag = fftData[2 * index + 1]
                    val magnitude = sqrt((real * real + imag * imag))
                    magnitudes[ch][idx] = 20 * log10(magnitude + 1e-6) // dB scale
                } else {
                    magnitudes[ch][idx] = 0.0
                }
            }
        }
        val flatArray = FloatArray(frequencyBands * channels)
        for (idx in 0 until channels) {
            for (jdx in 0 until frequencyBands) {
                flatArray[idx * (frequencyBands) + jdx] = magnitudes[idx][jdx].toFloat()
            }
        }
        return if (manager!!.isOpen) manager!!.create(flatArray, tensorShape) else null
    }

    private fun byteArrayToFloat(bytes: ByteArray, channels: Int): FloatArray {
        val channelPerBites = (sampleBit / 8)
        val channelSamples = FloatArray(bytes.size / channelPerBites)
        val perChannelSize = channelSamples.size / channels
        for (idx in 0 until perChannelSize) {
            for (jdx in 0 until channels) {
                val sampleBegIdx = idx * channels
                val low = bytes[sampleBegIdx + jdx * 2].toInt() and 0xFF
                val high = bytes[sampleBegIdx + jdx * 2 + 1].toInt()
                val sample = (high shl 8) or low
                // Normalize to [-1, 1], because it is 16 bit sound, sound use signed number
                val begIdx = jdx * perChannelSize
                channelSamples[begIdx + idx] += sample / 2.0.pow(sampleBit - 1.0).toFloat()
            }
        }
        return channelSamples
    }

    fun getAudioTensor(): NDArray? {
        return audioTensor
    }

    fun getAudioFromFile(filePath: String) {
        try {
            val file = File(filePath)
            val fileReader = MpegAudioFileReader()
            audioInputStream = fileReader.getAudioInputStream(file)
            val baseFormat = audioInputStream?.format
            val decodedFormat = AudioFormat(
                sampleRate,
                16,
                baseFormat!!.channels,
                true,
                false
            )
            audioInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)
            fileChannels = decodedFormat.channels
            fileSampleRate = decodedFormat.sampleRate
            fileBuffer = ByteArray(bufferSize * fileChannels * 2)
            fileSampleIndex = 0
            fileDoubleSamples = null
            fileBytesRead = 0
        } catch (e: Exception) {
            e.printStackTrace()
            audioInputStream = null
            fileBuffer = null
            fileDoubleSamples = null
            fileSampleIndex = 0
            fileBytesRead = 0
        }
    }

    fun getAudioTensorFromFile(): NDArray? {
        if (audioInputStream == null || fileBuffer == null) {
            return null
        }
        try {
            var offset = 0
            while (offset < fileBuffer!!.size) {
                val bytesRead = audioInputStream!!.read(fileBuffer!!, offset, fileBuffer!!.size - offset)
                if (bytesRead <= 0) {
                    audioInputStream?.close()
                    audioInputStream = null
                    fileBuffer = null
                    fileDoubleSamples = null
                    fileSampleIndex = 0
                    fileBytesRead = 0
                    return null
                }
                offset += bytesRead
            }
            fileBytesRead += offset
            val doubleSamples = byteArrayToFloat(fileBuffer!!, fileChannels)
            return processAudioSamples(doubleSamples)
        } catch (e: Exception) {
            e.printStackTrace()
            audioInputStream?.close()
            audioInputStream = null
            fileBuffer = null
            fileDoubleSamples = null
            fileSampleIndex = 0
            fileBytesRead = 0
            return null
        }
    }


    override fun close() {
        executor?.shutdown()
        audioLine?.stop()
        audioLine?.close()
        manager?.close()
        audioInputStream?.close()
    }
}
