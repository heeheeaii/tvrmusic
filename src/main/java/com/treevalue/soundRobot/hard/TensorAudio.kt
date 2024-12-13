package com.treevalue.soundRobot.hard

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDArrays
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import com.treevalue.soundRobot.hard.stft.STFT
import org.apache.commons.math3.complex.Complex
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class TensorAudio {
    fun audioToArrays(ais: AudioInputStream): Array<DoubleArray> {
        val format = ais.format
        assert(format.channels == 2) { "audio channel must is 2" }
        val frameSize = format.frameSize
        val frameLength = ais.frameLength
        val audioBytes = ByteArray((frameSize * frameLength).toInt())
        ais.read(audioBytes)
        val leftChannel = DoubleArray(frameLength.toInt())
        val rightChannel = DoubleArray(frameLength.toInt())
        for (i in 0 until frameLength) {
            leftChannel[i.toInt()] =
                (audioBytes[(i * frameSize).toInt()].toInt() and 0xFF or (audioBytes[(i * frameSize + 1).toInt()].toInt() shl 8)).toDouble()
            rightChannel[i.toInt()] =
                (audioBytes[(i * frameSize + 2).toInt()].toInt() and 0xFF or (audioBytes[(i * frameSize + 3).toInt()].toInt() shl 8)).toDouble()
        }
        return arrayOf(leftChannel, rightChannel)
    }

    fun audioToTensor(wav: File, manager: NDManager = TensorManager.getManager()): NDArray {
        val ais = AudioSystem.getAudioInputStream(wav)
        return audioToTensor(ais)
    }

    fun audioToTensor(wavPath: String, manager: NDManager = TensorManager.getManager()): NDArray {
        val ais = AudioSystem.getAudioInputStream(File(wavPath))
        return audioToTensor(ais)
    }

    fun audioToTensor(doubleChanel: Array<Array<Short>>, manager: NDManager = TensorManager.getManager()): NDArray {
        val leftFrequencyTensor = audioToTensor(doubleChanel[0])
        val rightFrequencyTensor = audioToTensor(doubleChanel[1])
        return NDArrays.concat(NDList(leftFrequencyTensor, rightFrequencyTensor), 0)
    }

    fun audioToTensor(ais: AudioInputStream, manager: NDManager = TensorManager.getManager()): NDArray {
        val audioArrays = audioToArrays(ais)
        val leftFrequencyTensor = audioToTensor(audioArrays[0])
        val rightFrequencyTensor = audioToTensor(audioArrays[1])
        return NDArrays.concat(NDList(leftFrequencyTensor, rightFrequencyTensor), 0)
    }

    fun audioToArrays(audioPath: String): Array<DoubleArray> {
        val ais = AudioSystem.getAudioInputStream(File(audioPath))
        return audioToArrays(ais)
    }

    fun audioToArrays(audio: File): Array<DoubleArray> {
        val ais = AudioSystem.getAudioInputStream(audio)
        return audioToArrays(ais)
    }

    fun arrayToTensor(array: Array<FloatArray>): NDArray {
        return TensorManager.create(array)
    }

    fun getLeftChannel(ais: AudioInputStream): DoubleArray {
        val leftAndRight = audioToArrays(ais)
        return leftAndRight[0]
    }

    fun getRightChannel(ais: AudioInputStream): DoubleArray {
        val leftAndRight = audioToArrays(ais)
        return leftAndRight[1]
    }

    fun doubleToFloatArray(data: DoubleArray): FloatArray {
        return data.map { it.toFloat() }.toFloatArray()
    }

    fun arrayToTensor(array: FloatArray): NDArray {
        return TensorManager.create(array)
    }

    fun arrayToTensor(array: DoubleArray): NDArray {
        return TensorManager.create(array)
    }

    /*
        return tensor with shape as (time, freq) with amplitude
     */

    fun audioToTensor(channel: Array<Short>): NDArray {
        val stft = STFT.stft(STFT.toDoubleArray(channel))
        return convertToTensor(stft)
    }

    fun audioToTensor(channel: DoubleArray): NDArray {
        val stft = STFT.stft(channel)
        return convertToTensor(stft)
    }

    private fun convertToTensor(
        stftResult: Array<Array<Complex>>, manager: NDManager = TensorManager.getManager()
    ): NDArray {
        val magnitudes = STFT.complexToFloatMatrix(stftResult)
        return manager.create(magnitudes).reshape(Shape(1, stftResult.size.toLong(), stftResult[0].size.toLong()))
    }

    fun getFrequency(row: Int, sampleFrequency: Int = 44100, windowSize: Int = STFT.WINDOW_SIZE): Double {
        assert(row < windowSize / 2) { "only [0 - flower(windowSize/2))" }
        return 1.0 * sampleFrequency * row / windowSize
    }
}
