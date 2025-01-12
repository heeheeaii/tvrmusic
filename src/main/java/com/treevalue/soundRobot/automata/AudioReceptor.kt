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

class AudioReceptor(
    private var manager: NDManager? = null,
    private var executor: ScheduledExecutorService? = null
) : Closeable {

    private val channels = 2 // Stereo
    private val sampleRate = 44100.0f // Standard sample rate
    private val bufferSize = 8820 // Number of samples for ~200ms window
    private val fft = DoubleFFT_1D(bufferSize.toLong())
    private val frequencyBands = 4406 // (22050 - 20) / 5
    private val tensorShape = Shape(channels.toLong(), frequencyBands.toLong())

    // Variables for file reading
    private var audioInputStream: AudioInputStream? = null
    private var fileBuffer: ByteArray? = null
    private var fileDoubleSamples: DoubleArray? = null
    private var fileSampleIndex = 0
    private var fileChannels = 0
    private var fileSampleRate = 0f

    @Volatile
    private var audioTensor: NDArray? = null

    private var audioLine: TargetDataLine? = null
    private val audioBuffer = ByteArray(bufferSize * channels * 2) // 16-bit audio

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
            16,
            channels,
            true,
            false
        )
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            throw IllegalArgumentException("Line not supported")
        }
        audioLine = AudioSystem.getLine(info) as TargetDataLine
        audioLine!!.open(format, bufferSize * channels * 2)
        audioLine!!.start()
    }

    private fun startCapture() {
        executor!!.scheduleAtFixedRate({
            try {
                val bytesRead = audioLine!!.read(audioBuffer, 0, audioBuffer.size)
                if (bytesRead > 0) {
                    val samples = ByteArray(bufferSize * channels * 2)
                    System.arraycopy(audioBuffer, 0, samples, 0, bytesRead)
                    val doubleSamples = byteArrayToDouble(samples, channels)

                    val magnitudes = Array(channels) { DoubleArray(frequencyBands) }
                    for (ch in 0 until channels) {
                        val fftData = DoubleArray(bufferSize)
                        System.arraycopy(
                            doubleSamples, 0, fftData, 0, fftData.size.coerceAtMost(doubleSamples.size)
                        )
                        fft.realForward(fftData)
                        for (i in 0 until frequencyBands) {
                            val freq = 20 + i * 5
                            val index = (freq * bufferSize / sampleRate).toInt()
                            if (index < bufferSize / 2) {
                                val real = fftData[2 * index]
                                val imag = fftData[2 * index + 1]
                                val magnitude = Math.sqrt((real * real + imag * imag))
                                magnitudes[ch][i] = 20 * log10(magnitude + 1e-6) // dB scale
                            } else {
                                magnitudes[ch][i] = 0.0
                            }
                        }
                    }

                    val flatMagnitudes = DoubleArray(channels * frequencyBands) {
                        if (it < magnitudes[0].size) magnitudes[0][it]
                        else magnitudes[1][it - magnitudes[0].size]
                    }
                    audioTensor = manager!!.create(flatMagnitudes, tensorShape)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, 5, TimeUnit.MILLISECONDS) // Update every 5 ms
    }

    private fun byteArrayToDouble(bytes: ByteArray, channels: Int): DoubleArray {
        val samples = DoubleArray(bytes.size / 2 / channels)
        for (i in samples.indices) {
            val idx = i * channels * 2
            for (ch in 0 until channels) {
                val low = bytes[idx + ch * 2].toInt() and 0xFF
                val high = bytes[idx + ch * 2 + 1].toInt()
                val sample = (high shl 8) or low
                samples[i] += sample / 32768.0 // Normalize to [-1, 1]
            }
            samples[i] = samples[i] / channels
        }
        return samples
    }

    fun getAudioTensor(): NDArray? {
        return audioTensor
    }

    private fun processAudioSamples(doubleSamples: DoubleArray): NDArray? {
        val magnitudes = Array(channels) { DoubleArray(frequencyBands) }
        for (ch in 0 until channels) {
            val fftData = DoubleArray(bufferSize)
            System.arraycopy(doubleSamples, 0, fftData, 0, fftData.size.coerceAtMost(doubleSamples.size))
            fft.realForward(fftData)
            for (i in 0 until frequencyBands) {
                val freq = 20 + i * 5
                val index = (freq * bufferSize / sampleRate).toInt()
                if (index < bufferSize / 2) {
                    val real = fftData[2 * index]
                    val imag = fftData[2 * index + 1]
                    val magnitude = Math.sqrt((real * real + imag * imag))
                    magnitudes[ch][i] = 20 * log10(magnitude + 1e-6) // dB scale
                } else {
                    magnitudes[ch][i] = 0.0
                }
            }
        }

        val flatMagnitudes = DoubleArray(channels * frequencyBands) {
            if (it < magnitudes[0].size) magnitudes[0][it]
            else magnitudes[1][it - magnitudes[0].size]
        }
        return manager!!.create(flatMagnitudes, tensorShape)
    }

    fun getAudioTensorFromFile(): NDArray? {
        if (audioInputStream == null || fileBuffer == null) {
            return null
        }
        try {
            val bytesRead = audioInputStream!!.read(fileBuffer!!, 0, fileBuffer!!.size)
            if (bytesRead <= 0) {
                audioInputStream?.close()
                audioInputStream = null
                fileBuffer = null
                fileDoubleSamples = null
                fileSampleIndex = 0
                return null
            }
            val samples = ByteArray(bytesRead)
            System.arraycopy(fileBuffer!!, 0, samples, 0, bytesRead)
            val doubleSamples = byteArrayToDouble(samples, fileChannels)
            return processAudioSamples(doubleSamples)
        } catch (e: Exception) {
            e.printStackTrace()
            audioInputStream?.close()
            audioInputStream = null
            fileBuffer = null
            fileDoubleSamples = null
            fileSampleIndex = 0
            return null
        }
    }

    fun getAudioFromFile(filePath: String) {
        try {
            val file = File(filePath)
            val fileReader = MpegAudioFileReader()
            audioInputStream = fileReader.getAudioInputStream(file)
            val baseFormat = audioInputStream!!.format
            val decodedFormat = AudioFormat(
                sampleRate,
                16,
                baseFormat.channels,
                true,
                false
            )
            audioInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)
            fileChannels = decodedFormat.channels
            fileSampleRate = decodedFormat.sampleRate
            fileBuffer = ByteArray(bufferSize * fileChannels * 2) // Buffer for file reading
            fileSampleIndex = 0
            fileDoubleSamples = null
        } catch (e: Exception) {
            e.printStackTrace()
            audioInputStream = null
            fileBuffer = null
            fileDoubleSamples = null
            fileSampleIndex = 0
        }
    }

    override fun close() {
        executor?.shutdown()
        audioLine?.stop()
        audioLine?.close()
        manager?.close()
    }
}
