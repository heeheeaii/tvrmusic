package com.treevalue.atsor.hard.stft

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object STFT {

    const val WINDOW_SIZE = 2048
    const val STEP_SIZE = 2048

    private fun generateTestSignal(length: Int): DoubleArray {
        val signal = DoubleArray(length)
        for (i in 0 until length) {
            signal[i] = sin(2 * Math.PI * i / 32) // First sine wave with a period of 32
            signal[i] += 0.5 * sin(2 * Math.PI * i / 8) // Second sine wave with a period of 8, half amplitude
        }
        return signal
    }

    inline fun <T : Number> toDoubleArray(array: Array<T>): DoubleArray {
        return array.map { it.toDouble() }.toDoubleArray()
    }

    /*
    Frequency resolution 44100/2048=21.5hz, temporal resolution 441002048=21.5, 1/21.5=50ms
    frequency = k * fs/windowSize (k belong 0-(windowSize-1)), actually only 0-windowSize/2
     */

    fun complexToFloatMatrix(origin: Array<Array<Complex>>): Array<FloatArray> {
        val length = origin.size
        val width = origin[0].size
        val magnitudes = Array(length) {
            FloatArray(
                width
            )
        }
        for (i in 0 until length) {
            for (j in 0 until width) {
                magnitudes[i][j] = origin[i][j].abs().toFloat()
            }
        }
        return magnitudes
    }

    fun stftToFloatMatrix(
        signal: DoubleArray,
        windowSize: Int = WINDOW_SIZE,
        stepSize: Int = STEP_SIZE,
        windowType: WindowType = WindowType.HAMMING
    ): Array<FloatArray> {
        return complexToFloatMatrix(stft(signal, windowSize, stepSize, windowType))
    }

    fun stft(
        signal: DoubleArray,
        windowSize: Int = WINDOW_SIZE,
        stepSize: Int = STEP_SIZE,
        windowType: WindowType = WindowType.HAMMING
    ): Array<Array<Complex>> {
        // Calculate the number of windows
        val numFrames = (signal.size - windowSize) / stepSize + 1
        // Initialize the result array
//        val result: Array<Array<Complex>> = Array(numFrames) { Array(windowSize) { Complex.ZERO } }
        val result: Array<Array<Complex>> = Array(numFrames) { arrayOf(Complex.ZERO) }
        // Create a FFT transformer
        val transformer = FastFourierTransformer(DftNormalization.STANDARD)

        // Process each window of the signal
        for (i in 0 until numFrames) {
            // Calculate the start position of the current window
            val start = i * stepSize
            // Slice the signal for the current window
            val windowedSignal = Arrays.copyOfRange(signal, start, start + windowSize)
            // Apply the selected window function
            applyWindow(windowedSignal, windowType)
            // Perform FFT on the windowed signal to get the spectrum
            result[i] = transformer.transform(windowedSignal, TransformType.FORWARD)
        }
        return result
    }

    private fun applyWindow(window: DoubleArray, windowType: WindowType) {
        when (windowType) {
            WindowType.HAMMING -> applyHammingWindow(window)
            WindowType.HANN -> applyHannWindow(window)
            WindowType.BLACKMAN -> applyBlackmanWindow(window)
            WindowType.RECTANGULAR -> applyRectangularWindow(window)
        }
    }

    // Applies the Hamming window function
    private fun applyHammingWindow(window: DoubleArray) {
        val length = window.size
        for (i in 0 until length) {
            window[i] *= 0.54 - 0.46 * cos(2 * Math.PI * i / (length - 1))
        }
    }

    // Applies the Hann window function
    private fun applyHannWindow(window: DoubleArray) {
        val length = window.size
        for (i in 0 until length) {
            window[i] *= 0.5 * (1 - cos(2 * Math.PI * i / (length - 1)))
        }
    }

    // Applies the Blackman window function
    private fun applyBlackmanWindow(window: DoubleArray) {
        val length = window.size
        for (i in 0 until length) {
            window[i] *= 0.42 - 0.5 * cos(2 * Math.PI * i / (length - 1)) + 0.08 * cos(4 * Math.PI * i / (length - 1))
        }
    }

    // Applies the Rectangular window function (does not modify the input signal)
    private fun applyRectangularWindow(window: DoubleArray) {
    }
}
