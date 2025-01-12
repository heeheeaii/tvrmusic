package com.treevalue.soundRobot.autom

import AudioReceptor
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AudioTest {
    @Test
    fun audioCardTest() {
        AudioReceptor().use { audioReceptor ->
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleAtFixedRate({
                val tensor = audioReceptor.getAudioTensor()
                if (tensor != null) {
                    println("Audio Tensor Shape: ${tensor.shape}")
                }
            }, 0, 100, TimeUnit.MILLISECONDS)

            Thread.sleep(10_00)
            executor.shutdown()
        }
    }

    @Test
    fun fileTest() {
        AudioReceptor().use { audioReceptor ->
            val executor = Executors.newSingleThreadScheduledExecutor()
            audioReceptor.getAudioFromFile("D:\\agi\\tvrmusicnew\\src\\main\\resources\\static\\music\\Andare - Ludovico Einaudi.mp3")
            var counter = 0
            var tensor = audioReceptor.getAudioTensorFromFile()
            while (tensor != null) {
                counter++
                tensor = audioReceptor.getAudioTensorFromFile()
            }
            println("total $counter")
            executor.shutdown()
        }
    }
}
