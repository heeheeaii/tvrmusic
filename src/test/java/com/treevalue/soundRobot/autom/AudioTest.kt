package com.treevalue.soundRobot.autom

import AudioReceptor
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AudioTest {
    @Test
    fun t1() {
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
}
