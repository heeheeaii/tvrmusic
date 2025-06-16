package com.treevalue.quick

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Represents a growing medium for neurons, managing their positions and expansion.
 * The layer is a cube centered at (0,0,0).
 * 神经元附着面
 * @property layerIdx idx in tranform order
 * @property initialHalfSideLength The initial half-side length of the cubic layer boundary.
 * @property maxHalfSideLength The maximum half-side length the layer can expand to. Use Float.POSITIVE_INFINITY for unlimited spatial growth.
 * @property growthThreshold The occupancy threshold (fraction of maxNeuronCount) that triggers an expansion attempt. Only applies if maxNeuronCount is finite. Defaults to 0.85f.
 * @property defaultGrowthFactor The default factor by which the layer tries to expand its half-side length (e.g., 1.5 means 1.5x). Defaults to 1.5f.
 * @property signalTransmissionManager The EventHandler instance used by newly created neurons.
 */
open class Layer(
    val layerIdx: Int = 0,
    val initialHalfSideLength: Float = 10.0f, // +,- scale
    val maxHalfSideLength: Float = 50f,
//    val maxHalfSideLength: Float = Float.POSITIVE_INFINITY,
    val growthThreshold: Float = 0.85f,
    private val defaultGrowthFactor: Float = 1.5f,
    private val signalTransmissionManager: SignalTransmissionManager
) {
    init {
        require(initialHalfSideLength > 0) { "Initial half-side length must be positive." }
        require(maxHalfSideLength >= initialHalfSideLength) { "Max half-side length must be >= initial half-side length." }
        require(growthThreshold in 0.0f..1.0f) { "Growth threshold must be between 0.0 and 1.0." }
        require(defaultGrowthFactor > 1.0f) { "Default growth factor must be greater than 1.0." }
    }

    companion object {
        private var instance: Layer? = null

        fun getInstance(
            layerIdx: Int = 0,
            initialHalfSideLength: Float = 10.0f,
            maxHalfSideLength: Float = 50f,
            growthThreshold: Float = 0.85f,
            defaultGrowthFactor: Float = 1.5f,
            signalTransmissionManager: SignalTransmissionManager = SignalTransmissionManager.getInstance()
        ): Layer {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = Layer(
                            layerIdx,
                            initialHalfSideLength,
                            maxHalfSideLength,
                            growthThreshold,
                            defaultGrowthFactor,
                            signalTransmissionManager
                        )
                    }
                }
            }
            return instance!!
        }
    }

    private val neurons: ConcurrentHashMap<Position, Neuron> = ConcurrentHashMap()
    private val currentHalfSideLength: AtomicReference<Float> = AtomicReference(initialHalfSideLength)
    private val expansionLock = ReentrantLock()
    private val growthFactors = listOf(defaultGrowthFactor, 1.4f, 1.3f, 1.2f, 1.1f)

    private val timeCounter = AtomicReference(0f)

    /** Gets the next unique sequence number for events/memories within this layer. */
    fun timeNext() {
        while (true) {
            val curSeq = timeCounter.get()
            val newValue = curSeq + 1
            if (timeCounter.compareAndSet(curSeq, newValue)) {
                break
            }
        }
    }

    /** Gets the current global sequence number without incrementing. */
    fun getCurTimeSeq(): Float {
        return timeCounter.get()
    }

    fun has(position: Position): Boolean {
        val currentBounds = currentHalfSideLength.get()
        return isWithinBounds(position, currentBounds) && neurons.containsKey(position)
    }

    fun getNeuron(position: Position): Neuron? {
        val currentBounds = currentHalfSideLength.get()
        return if (isWithinBounds(position, currentBounds)) {
            neurons[position]
        } else {
            null
        }
    }

    /**
     * Creates and places a new neuron at a specific position, connecting it to a source.
     * This method is called by the GrowthManager ONLY when a growth event is complete.
     *
     * @param newPosition The target position for the new neuron.
     * @param source other neuron , may be from outer
     * @return The newly created Neuron, or null if a neuron already exists there.
     */
    fun connectNeuronTo(source: Neuron, newPosition: Position): Neuron? {
        val currentBounds = currentHalfSideLength.get()
        if (!isWithinBounds(newPosition, currentBounds)) {
            return null
        }

        val newNeuron =
            Neuron(coordinate = newPosition, signalTransmissionManager = signalTransmissionManager, layer = this)
        val existingNeuron = neurons.putIfAbsent(newPosition, newNeuron)

        return if (existingNeuron == null) {
            source.connect(newNeuron.coordinate)

            if (needsExpansion()) {
                tryExpand()
            }
            newNeuron
        } else {
            source.connect(existingNeuron.coordinate)
            null
        }
    }

    /**
     * 在to生长一个神经元，如果有from的神经元从from连向to
     * @param to The target position for the new neuron.
     * @param fromNeuron Optional: The neuron initiating the growth (used to establish initial connections).
     * @return The newly created Neuron if growth was successful, null otherwise.
     */
    fun growTo(to: Position, fromNeuron: Neuron? = null): Neuron? {
        val currentBounds = currentHalfSideLength.get()
        if (!isWithinBounds(to, currentBounds)) {
            return null
        }
        val newNeuron =
            Neuron(coordinate = to, signalTransmissionManager = signalTransmissionManager, layer = this)
        val existingNeuron = neurons.putIfAbsent(to, newNeuron)
        if (existingNeuron == null) {
            fromNeuron?.connect(newNeuron.coordinate)
            if (needsExpansion()) {
                tryExpand()
            }
            return newNeuron
        } else {
            return null
        }
    }

    fun getNeuronCount(): Int = neurons.size

    fun getCurrentHalfSideLength(): Float = currentHalfSideLength.get()
    fun getAllNeurons(): Collection<Neuron> = neurons.values.toList()
    private fun isWithinBounds(position: Position, halfSide: Float): Boolean {
        return abs(position.x) <= halfSide &&
                abs(position.y) <= halfSide
    }

    private fun needsExpansion(): Boolean {
        val currentSize = neurons.size
        val maxNeuronCount = maxHalfSideLength.toDouble().pow(3.0).toFloat()
        val thresholdCount = (growthThreshold * maxNeuronCount).toLong()
        val currentBounds = currentHalfSideLength.get()
        val canGrowSpatially = currentBounds < maxHalfSideLength
        return currentSize >= thresholdCount && canGrowSpatially
    }

    private fun tryExpand(): Boolean {
        if (!expansionLock.tryLock()) {
            return false
        }
        try {
            if (!needsExpansion()) {
                return false
            }
            val currentBounds = currentHalfSideLength.get()
            var expansionSuccessful = false
            for (factor in growthFactors) {
                val potentialNewHalfSide = currentBounds * factor
                val targetHalfSide = min(potentialNewHalfSide, maxHalfSideLength)
                if (targetHalfSide > currentBounds) {
                    if (currentHalfSideLength.compareAndSet(currentBounds, targetHalfSide)) {
                        println("Layer expanded from $currentBounds to $targetHalfSide (Factor: $factor).")
                        expansionSuccessful = true
                        break
                    } else {
                        System.err.println("WARN: Layer expansion CAS failed. Current bounds changed concurrently.")
                        expansionSuccessful = false
                        break
                    }
                } else {
                }
            }
            if (!expansionSuccessful && currentBounds < maxHalfSideLength) {
                println("Layer expansion failed to find suitable factor or CAS failed. Current bounds: $currentBounds")
            } else if (!expansionSuccessful && currentBounds >= maxHalfSideLength) {
                println("Layer expansion not possible: Already at maximum spatial bounds ($maxHalfSideLength).")
            }
            return expansionSuccessful
        } finally {
            expansionLock.unlock()
        }
    }

    override fun toString(): String {
        return "Layer(currentHalfSide=${currentHalfSideLength.get()}, maxHalfSide=$maxHalfSideLength, neuronCount=${neurons.size}, sequence=${timeCounter.get()})"
    }

    fun getRandomPosition(number: Int): Array<Position> {
        if (number <= 0) {
            return arrayOf()
        }
        val positions = mutableListOf<Position>()
        val currentBounds = currentHalfSideLength.get()
        val random = Random(System.nanoTime())

        for (i in 0 until number) {
            val x = random.nextFloat() * (2 * currentBounds) - currentBounds
            val y = random.nextFloat() * (2 * currentBounds) - currentBounds
            val z = layerIdx
            positions.add(Position(x.toInt(), y.toInt(), z.toInt()))
        }

        return positions.toTypedArray()
    }
}
