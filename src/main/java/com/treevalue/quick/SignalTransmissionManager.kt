package com.treevalue.quick

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.exp

/**
 * 神经元信号传播处理器
 */
class SignalTransmissionManager {
    private val decayRate = 0.1f

    companion object {
        private var instance: SignalTransmissionManager? = null

        fun getInstance(): SignalTransmissionManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SignalTransmissionManager()
                    }
                }
            }
            return instance!!
        }
    }

    private val eventQueue = ConcurrentLinkedQueue<SignalEvent>()
    private val processingExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "EventHandlerThread").apply { isDaemon = true }
    }
    private val isRunning = AtomicBoolean(false)

    fun submitEvent(event: SignalEvent) {
        eventQueue.offer(event)
    }

    fun startProcessing() {
        if (isRunning.compareAndSet(false, true)) {
            processingExecutor.submit {
                while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                    processBatch()
                    if (eventQueue.isEmpty()) {
                        try {
                            Thread.sleep(0, 100_000)
                        } catch (ie: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                }
            }
        }
    }


    private fun processBatch() {
        var processedCount = 0
        val batchSize = 500

        while (processedCount < batchSize) {
            val event = eventQueue.poll() ?: break

            try {
                processSingleEvent(event)
                processedCount++
            } catch (e: Exception) {
                try {
                    event.signal.close()
                } catch (closeE: Exception) { /* Ignore closing error */
                }
            }
        }
    }


    private fun calculateDecayFactor(distance: Float): Float {
        val nonNegativeDistance = if (distance < 0f) 0f else distance
        return exp(-decayRate * nonNegativeDistance)
    }

    private fun processSingleEvent(event: SignalEvent) {
        val targetNeuron = event.targetNeuron
        val decayFactor = calculateDecayFactor(event.distance)
        try {
            val originalStrength = event.signal.getFloat(SIGNAL_EXCITATORY_IDX)
            event.signal.putScalar(
                intArrayOf(0, SIGNAL_EXCITATORY_IDX),
                originalStrength * decayFactor
            )
            targetNeuron.receive(event)
        } finally {
            event.signal.close()
        }
    }


    fun stopProcessing() {
        isRunning.set(false)
        processingExecutor.shutdown()
        try {
            if (!processingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow()
            }
        } catch (ie: InterruptedException) {
            processingExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        var cleanedCount = 0
        while (true) {
            val event = eventQueue.poll() ?: break
            try {
                event.signal.close()
            } catch (e: Exception) {/* ignore */
            }
            cleanedCount++
        }
    }
}
