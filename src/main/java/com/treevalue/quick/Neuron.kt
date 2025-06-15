package com.treevalue.quick

import MemoryList
import com.treevalue.atsor.data.RangedKeyDataStore
import kotlin.random.Random
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.util.concurrent.*
import kotlin.math.exp

class Neuron(
    val coordinate: Position, private val signalTransmissionManager: SignalTransmissionManager, private val layer: Layer = Layer.getInstance()
) {

    private val connects: ConcurrentHashMap<Position, Float> = ConcurrentHashMap()
    private val inSignals = RangedKeyDataStore.of<Float, INDArray>(25f) // 25 ms
    private val memories = MemoryList()
    private val shortTermMemoryTranslation: Float = 0f
    private val longTermMemoryTranslation: Float = 0.5f
    private val fixedMemoryThreshold: Int = 20
    private val longTermMemoryThreshold: Int = 3

    fun connect(position: Position) {
        if (position != coordinate) {
            val distance = Position.distanceOf(this.coordinate, position)
            connects[position] = distance
        }
    }

    /**
     * @param distance logic distance, of neron spacial distance in coordination
     * @param k decay factor, default 0.001
     */
    private fun signalDecay(strength: INDArray, distance: Float, k: Float = 0.001f) {
        val decayFactor = exp(-k * distance.toDouble())
        strength.muli(decayFactor)
    }


    fun receive(event: SignalEvent) {
        signalDecay(event.signal, Position.distanceOf(event.sourceNeuron.coordinate, coordinate))
        inSignals.put(event.timeSeq, event.signal)
    }

    fun processReceivedSignals(timeSequence: Float) {
        val signalsToProcess = inSignals.getAllAndClear(timeSequence)
        if (signalsToProcess.isEmpty()) {
            return
        }
        val sumSignal: INDArray = Nd4j.create(SIGNAL_LENGTH)
        signalsToProcess.forEach {
            sumSignal.addi(it)
        }
        if (sumSignal.getFloat(SIGNAL_EXCITATORY_IDX) > ACTIVE_THRESHOLD) {
            active(timeSequence, sumSignal)
        }
    }

    private fun active(modelKey: Float, triggeringSignal: INDArray) {
        val timeSeq = layer.getCurTimeSeq()
        val outgoingSignalBase = triggeringSignal.dup()

        val outputMap: MutableMap<Position, INDArray> = ConcurrentHashMap()
        connects.forEach { (position, distance) ->
            val signalToSend = outgoingSignalBase.dup()
            signalDecay(signalToSend, distance, 0.001f)
            outputMap[position] = signalToSend
        }

        outputMap.forEach { (position, signal) ->
            val targetNeuron = layer.getNeuron(position)
            targetNeuron?.let {
                val event = SignalEvent(
                    sourceNeuron = this,
                    targetNeuron = it,
                    signal = signal,
                    distance = connects[position]!!,
                    timeSeq = timeSeq
                )
                signalTransmissionManager.submitEvent(event)
            }
        }

        memories.putToHead(
            modelKey, Memory(
                output = outputMap.toMutableMap(),
                reinforcementCount = 0,
            )
        )
        memories.first()?.let {
            trySolidifyMemory(it)
        }
    }

    private fun trySolidifyMemory(memory: Memory?) {
        memory?.let {
            if (it.isFixed) {
                return
            }
            it.reinforcementCount++
            if (!it.isLongTerm && it.reinforcementCount >= longTermMemoryThreshold) {
                it.rememberLonRemember()
            } else if (it.isLongTerm && !it.isFixed && it.reinforcementCount >= fixedMemoryThreshold) {
                it.rememberForever()
            }
        }
    }

    /**
     * Ebbinghaus-inspired forgetting mechanism.
     * Iterates through memories and removes some based on calculated retention probability.
     */
    private fun naturalForget() {
        val curTimeSeq: Float = layer.getCurTimeSeq()
        if (memories.isEmpty()) return
        var forgottenCount = 0
        val forgetModelKey = mutableListOf<Float>()
        for ((modelKey, memory) in memories) {
            if (memory.isFixed) {
                continue
            }
            val timeElapsed = curTimeSeq - modelKey
            if (timeElapsed <= 0) {
                continue
            }
            val translation = if (memory.isLongTerm) longTermMemoryTranslation else shortTermMemoryTranslation
            val retention = calculateRetention(timeElapsed, memory.reinforcementCount, translation)
            val forgetProbability = 1.0f - retention
            if (Random.nextFloat() < forgetProbability) {
                forgetModelKey.add(modelKey)
            }
        }

        forgetModelKey.forEach {
            memories.remove(it)
        }
    }

    /**
     * Calculates memory retention based on Ebbinghaus-like formula.
     * R = exp(-(timeElapsed + translation) / Strength)
     * Strength increases with reinforcement.
     * @param timeElapsed ms
     *  @param translation 平移
     */
    private fun calculateRetention(timeElapsed: Float, reinforcement: Int, translation: Float): Float {
        if (reinforcement <= 0f) return 0f
        if (timeElapsed <= 0f) return 1f
        val msToSec = 1000
        return (exp(-timeElapsed / (reinforcement * msToSec)) + translation).coerceIn(0.0f, 1.0f)
    }

    fun getMemoryCount(): Int = memories.size

    override fun toString(): String {

        val connectionCount = connects.size
        return "Neuron(coordinate=$coordinate, connections=$connectionCount, memories=${memories.size})"
    }

}
