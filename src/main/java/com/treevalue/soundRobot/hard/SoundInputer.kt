package com.treevalue.soundRobot.hard

import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class SoundInputer(val musicPath: String) : TensorInputer {
    private lateinit var inputor: TensorI
    lateinit var audioOperator: TensorAudio
    private var nameList: MutableList<String> = mutableListOf()
    var tensor: TensorI? = null
    private var counter: Int = 0

    init {
        nameList = getFileListFrom(musicPath).toMutableList() // Convert to MutableList
        CoroutineScope(Dispatchers.IO).launch { mp3ToWav() } // Use CoroutineScope for launching coroutine
    }

    suspend fun tensorMonitor() {
        if (tensor == null) {
            while (counter < nameList.size) {
                val currentFileName = nameList[counter]
                if (isMp3End(currentFileName)) {
                    tensor = audioOperator.audioToTensor("$musicPath/$currentFileName")
                    break
                } else {
                    counter++
                }
            }
            if (counter >= nameList.size) counter = 0 // Reset counter if end of list is reached
        }
    }


    private suspend fun mp3ToWav() {
        for (fileName in nameList.toList()) {
            if (isMp3End(fileName) && !nameList.contains(mp3EndChangeToWav(fileName))) {
                val wav = mp3ToWav("$musicPath/$fileName")
                writeTo(wav, musicPath)
            }
        }
    }


    override fun getTensor(): TensorI {
        if (tensor == null) {
            var hasCycle = false
            var preCounter = counter - 1
            while (counter < nameList.size) {
                if (isWavEnd(nameList[counter])) {
                    val tensorTmp = audioOperator.audioToTensor("$musicPath/${nameList[counter]}")
                    counter++
                    return tensorTmp
                } else {
                    nameList.removeAt(counter) // Use removeAt for MutableList
                    if (hasCycle && counter == preCounter) {
                        break
                    }
                    if (counter >= nameList.size) {
                        if (!hasCycle) {
                            counter = 0
                            hasCycle = true
                            preCounter = nameList.size - 1

                        }
                    }
                }
            }
            if (tensor == null && nameList.isNotEmpty()) {
                counter = 0
                return audioOperator.audioToTensor("$musicPath/${nameList[0]}") // Or handle differently
            }

        }
        return tensor!!
    }

    private fun getFileListFrom(path: String): List<String> = File(path).listFiles()?.map { it.name } ?: emptyList()
    private fun isMp3End(fileName: String): Boolean = fileName.endsWith(".mp3")
    private fun isWavEnd(fileName: String): Boolean = fileName.endsWith(".wav")
    private fun mp3EndChangeToWav(fileName: String): String = fileName.replace(".mp3", ".wav")

    private fun mp3ToWav(filePath: String): ByteArray {
        return try {
            val mp3File = File(filePath)
            val audioInputStream = AudioSystem.getAudioInputStream(mp3File)
            val format = audioInputStream.format

            val pcmFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.sampleRate,
                16,
                format.channels,
                format.channels * 2,
                format.sampleRate,
                false
            )

            val pcmStream = AudioSystem.getAudioInputStream(pcmFormat, audioInputStream)

            val baos = ByteArrayOutputStream()
            AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, baos)

            baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }
    }

    private fun writeTo(wav: ByteArray, path: String) {
        try {
            val file = File(path)
            val parentDir = file.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }

            FileOutputStream(file).use { outputStream ->
                outputStream.write(wav)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
