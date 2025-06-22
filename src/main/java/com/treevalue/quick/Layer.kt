package com.treevalue.quick

import com.treevalue.quick.data.WHShape
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
    val initialHalfSideLength: Int = 16, // +,- scale
    val maxHalfSideLength: Int = 16,
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


    fun getShape(): WHShape = WHShape(initialHalfSideLength * 2, initialHalfSideLength * 2)

    companion object {
        private var instance: Layer? = null

        fun getInstance(
            layerIdx: Int = 0,
            initialHalfSideLength: Int = 10,
            maxHalfSideLength: Int = 50,
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
    private val currentHalfSideLength: AtomicReference<Int> = AtomicReference(initialHalfSideLength)
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
        var toNeuron: Neuron? = neurons[to]
        toNeuron?.let {
            toNeuron = Neuron(coordinate = to, signalTransmissionManager = signalTransmissionManager, layer = this)
            neurons[to] = toNeuron!!
        }

        fromNeuron?.connect(toNeuron!!.coordinate)
        if (needsExpansion()) {
            tryExpand()
        }
        return toNeuron
    }

    fun getNeuronCount(): Int = neurons.size

    fun getCurrentHalfSideLength(): Int = currentHalfSideLength.get()

    fun getAllNeurons(): Collection<Neuron> = neurons.values.toList()
    private fun isWithinBounds(position: Position, halfSide: Int): Boolean {
        return abs(position.x) <= halfSide && abs(position.y) <= halfSide
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

                val targetHalfSide = min(potentialNewHalfSide.toInt(), maxHalfSideLength)
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

    fun getRandom(number: Int): List<Pair<Int, Int>> {
        val currentBounds = currentHalfSideLength.get()

        val sideLength = (2L * currentBounds + 1)
        val maxPossiblePositions = sideLength * sideLength
        if (number <= 0 || number > maxPossiblePositions) {
            return emptyList()
        }
        val interval = (maxPossiblePositions.toDouble() / number).toInt()

        val positions = mutableListOf<Pair<Int, Int>>()
        val random = Random.Default
        for (i in 0L until number) {
            val startIndex = i * interval.toLong()
            val endIndex = min((i + 1) * interval, maxPossiblePositions)
            val randomIndexInInterval = random.nextLong(startIndex, endIndex)
            val x = (randomIndexInInterval / sideLength).toInt() - currentBounds
            val y = (randomIndexInInterval % sideLength).toInt() - currentBounds
            positions.add(x to y)
        }
        return positions
    }

    fun getRandomPosition(number: Int): List<Position> {
        val currentBounds = currentHalfSideLength.get()
        val currentLayerZ = layerIdx

        val sideLength = (2L * currentBounds + 1)
        val maxPossiblePositions = sideLength * sideLength

        if (number <= 0 || number > maxPossiblePositions) {
            return emptyList()
        }

        val interval = (maxPossiblePositions.toDouble() / number).toLong()

        val positions = mutableListOf<Position>()
        val random = Random.Default
        val generatedIndices = mutableSetOf<Long>()

        var count = 0
        while (count < number) {
            val theoreticalStartIndex = count * interval
            val theoreticalEndIndex = (count + 1) * interval

            val actualEndIndexForSample = min(theoreticalEndIndex, maxPossiblePositions)

            val randomIndexInInterval = random.nextLong(theoreticalStartIndex, actualEndIndexForSample)

            if (!generatedIndices.add(randomIndexInInterval)) {
                continue
            }

            val x = (randomIndexInInterval / sideLength).toInt() - currentBounds
            val y = (randomIndexInInterval % sideLength).toInt() - currentBounds

            positions.add(Position(x, y, currentLayerZ))
            count++
        }

        return positions
    }
}
