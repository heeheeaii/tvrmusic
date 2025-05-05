package com.treevalue.quick

import kotlinx.coroutines.*
import kotlin.random.Random
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.exp
import kotlin.math.sqrt


data class Position(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: Position): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z

        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}


const val SIGNAL_EXCITATORY_INHIBITORY_IDX = 0
const val SIGNAL_REWARD_IDX = 1
const val SIGNAL_VECTOR_SIZE = 2

const val FIRING_THRESHOLD = 0.8f
const val DEFAULT_FIRING_SIGNAL_STRENGTH = 1.0f

class Neuron(
    val coordinate: Position,
    private val eventHandler: EventHandler
) {


    private val sendTo: MutableMap<Neuron, Float> = mutableMapOf()


    private val incomingSignalBuffer = AtomicReference(ConcurrentLinkedQueue<INDArray>())


    /**
     * Adds a connection to a downstream neuron.
     * Usually called during network setup.
     */
    fun addConnection(targetNeuron: Neuron) {
        if (targetNeuron != this) {
            val distance = this.coordinate.distanceTo(targetNeuron.coordinate)
            sendTo[targetNeuron] = distance
        }
    }

    /**
     * Receives an incoming signal tensor. Thread-safe and fast.
     */
    fun receive(tensor: INDArray) {

        if (tensor.size(1) == SIGNAL_VECTOR_SIZE.toLong() && tensor.rank() == 2 && tensor.size(0) == 1L) {
            incomingSignalBuffer.get().offer(tensor.dup())
        } else {
            System.err.println("Neuron at ${this.coordinate} received malformed signal: shape ${tensor.shapeInfoToString()}")
        }
    }

    /**
     * Processes all signals received in the last cycle. Called periodically.
     */
    fun processReceivedSignals() {

        val signalsToProcess = incomingSignalBuffer.getAndSet(ConcurrentLinkedQueue())

        if (signalsToProcess.isEmpty()) {
            return
        }

        var excitatoryInhibitorySum = 0.0f
        var rewardSum = 0.0f


        while (true) {
            val signal = signalsToProcess.poll() ?: break
            try {
                excitatoryInhibitorySum += signal.getFloat(SIGNAL_EXCITATORY_INHIBITORY_IDX)
                rewardSum += signal.getFloat(SIGNAL_REWARD_IDX)
            } catch (e: Exception) {

                System.err.println("Neuron at ${this.coordinate} error processing signal content: ${signal}. Error: ${e.message}")
            }
        }


        if (excitatoryInhibitorySum > FIRING_THRESHOLD) {
            fire(excitatoryInhibitorySum, rewardSum)
        }
    }


    /**
     * Generates and sends firing events to the EventHandler for connected neurons.
     */
    private fun fire(triggerStrength: Float, accumulatedReward: Float) {

        val outgoingSignal = Nd4j.create(
            floatArrayOf(DEFAULT_FIRING_SIGNAL_STRENGTH, 0.0f),
            1L,
            SIGNAL_VECTOR_SIZE.toLong()
        )


        sendTo.forEach { (targetNeuron, distance) ->
            val event = SignalEvent(
                sourceNeuron = this,
                targetNeuron = targetNeuron,
                signal = outgoingSignal,
                distance = distance
            )
            eventHandler.submitEvent(event)
        }
    }


    override fun toString(): String {

        return "Neuron(coordinate=$coordinate, connections=${sendTo.size})"
    }
}

/**
 * Represents a signal transmission event. Uses Neuron object references.
 */
data class SignalEvent(
    val sourceNeuron: Neuron,
    val targetNeuron: Neuron,
    val signal: INDArray,
    val distance: Float
)


const val DECAY_CONSTANT = 0.1f

fun calculateDecayFactor(distance: Float): Float {
    val nonNegativeDistance = if (distance < 0f) 0f else distance
    return exp(-DECAY_CONSTANT * nonNegativeDistance)
}


class EventHandler {

    private val eventQueue = ConcurrentLinkedQueue<SignalEvent>()

    private val processingExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "EventHandlerThread").apply { isDaemon = true }
    }
    private val isRunning = AtomicBoolean(false)

    /**
     * Neurons call this method to submit a firing event. Thread-safe.
     */
    fun submitEvent(event: SignalEvent) {
        eventQueue.offer(event)
    }

    /**
     * Starts the event processing loop in a background thread.
     */
    fun startProcessing() {
        if (isRunning.compareAndSet(false, true)) {
            processingExecutor.submit {

                while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                    processBatch()

                    if (eventQueue.isEmpty()) {
                        try {

                            Thread.sleep(0, 50_000)
                        } catch (ie: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }

            }
        }
    }

    /**
     * Processes a batch of events currently in the queue.
     */
    private fun processBatch() {
        var processedCount = 0
        val batchSize = 1000

        while (processedCount < batchSize) {
            val event = eventQueue.poll() ?: break

            try {
                processSingleEvent(event)
                processedCount++
            } catch (e: Exception) {
                System.err.println("EventHandler error processing event for target ${event.targetNeuron.coordinate}. Error: ${e.message}")

            }
        }
    }

    /**
     * Processes a single signal event: calculates decay, calls receive on target.
     */
    private fun processSingleEvent(event: SignalEvent) {
        val targetNeuron = event.targetNeuron
        val decayFactor = calculateDecayFactor(event.distance)
        val decayedSignalTensor = event.signal.dup()
        val originalStrength = decayedSignalTensor.getFloat(SIGNAL_EXCITATORY_INHIBITORY_IDX)
        decayedSignalTensor.putScalar(intArrayOf(0, SIGNAL_EXCITATORY_INHIBITORY_IDX), originalStrength * decayFactor)
        targetNeuron.receive(decayedSignalTensor)


    }


    /**
     * Stops the event processing loop gracefully.
     */
    fun stopProcessing() {
        isRunning.set(false)
        processingExecutor.shutdown()
        try {
            if (!processingExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.err.println("EventHandler shutdown timed out, forcing.")
                processingExecutor.shutdownNow()
            }
        } catch (ie: InterruptedException) {
            processingExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}


fun main() = runBlocking {
    println("Setting up neural simulation...")

    val eventHandler = EventHandler()

    val numNeurons = 100
    val neurons = List(numNeurons) {
        Neuron(
            coordinate = Position(
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100
            ),
            eventHandler = eventHandler
        )
    }
    println("Created ${neurons.size} neurons.")


    val connectionsPerNeuron = 5
    neurons.forEach { sourceNeuron ->
        repeat(connectionsPerNeuron) {
            var targetNeuron: Neuron
            do {

                targetNeuron = neurons.random()
            } while (targetNeuron == sourceNeuron)
            sourceNeuron.addConnection(targetNeuron)
        }
    }
    println("Created connections.")

    eventHandler.startProcessing()

    val simulationDurationMs = 100L
    val tickDurationMs = 1L

    println("Starting simulation for $simulationDurationMs ms...")
    val startTime = System.currentTimeMillis()

    val simulationJob = launch(Dispatchers.Default) {
        for (tick in 1..simulationDurationMs) {
            val tickStart = System.nanoTime()

            if (tick % 10 == 1L) {
                val targetNeuron = neurons.random()
                val externalSignal =
                    Nd4j.create(floatArrayOf(0.9f, 0.0f), 1L, SIGNAL_VECTOR_SIZE.toLong())
                targetNeuron.receive(externalSignal)
            }

            delay(0)

            neurons.forEach { neuron ->
                neuron.processReceivedSignals()
            }

            val tickEnd = System.nanoTime()
            val elapsedNs = tickEnd - tickStart
            val delayNs = (tickDurationMs * 1_000_000) - elapsedNs
            if (delayNs > 0) {
                delay(delayNs / 1_000_000)
            } else if (elapsedNs > tickDurationMs * 1_000_000 * 1.1) {
                println("Warning: Tick $tick took longer than ${tickDurationMs}ms (${elapsedNs / 1_000_000.0}ms)")
            }

            if (tick % 50 == 0L || tick == simulationDurationMs) println("Tick $tick completed.")
        }
    }


    simulationJob.join()

    val endTime = System.currentTimeMillis()
    println("Simulation finished after ${endTime - startTime} ms wall clock time.")


    println("Stopping EventHandler...")
    eventHandler.stopProcessing()
    println("Cleanup complete.")
}
