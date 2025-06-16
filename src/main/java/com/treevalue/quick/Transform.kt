package com.treevalue.quick

import com.treevalue.quick.layer.*
import org.nd4j.linalg.api.ndarray.INDArray
import java.util.UUID

class Transform {
    private val signalTransmissionManager: SignalTransmissionManager = SignalTransmissionManager.getInstance()
    private val feelingLayer: FeelingLayer = FeelingLayer(signalTransmissionManager = signalTransmissionManager)
    private val shallowLayer: ShallowLayer = ShallowLayer(signalTransmissionManager = signalTransmissionManager)
    private val deepLayer: DeepLayer = DeepLayer(signalTransmissionManager = signalTransmissionManager)
    private val outputLayer: OutputLayer = OutputLayer(signalTransmissionManager = signalTransmissionManager)
    private val feedbackLayer: FeedbackLayer = FeedbackLayer(signalTransmissionManager = signalTransmissionManager)
    private val layerMap: Map<Int, Layer> = mapOf(
        0 to feelingLayer,
        1 to shallowLayer,
        2 to deepLayer,
        3 to outputLayer,
        4 to feedbackLayer
    )

    init {
        signalTransmissionManager.startProcessing()
    }

    fun input(feeling: INDArray) {
        feelingLayer.input(feeling)
    }

    fun connectNeuronTo(sourceNeuron: Neuron, targetPosition: Position): Neuron? {
        val layerIndex = targetPosition.z.toInt()
        return layerMap[layerIndex]?.connectNeuronTo(sourceNeuron, targetPosition)
    }

    fun getNeuron(position: Position): Neuron? {
        val layerIndex = position.z
        return layerMap[layerIndex]?.getNeuron(position)
    }

    fun except(feeling: List<Position>, except: List<Position>) {
        feeling.forEach {
            feelingLayer.growTo(it)
        }
        val match = GrowthManager.getInstance().getInOutMatch(feeling, except)
        match.forEach { (inIdx, outIdx) ->
            GrowthManager.getInstance().requestGrowth(feelingLayer.getNeuron(feeling[inIdx]), except[outIdx])
        }
    }

    fun predicate(input: INDArray): Pair<UUID, INDArray> {
        TODO("Not yet implemented")
    }

    companion object {
        @Volatile
        private var instance: Transform? = null
        fun getInstance(): Transform = instance ?: synchronized(this) {
            instance ?: Transform().also { instance = it }
        }
    }

    fun getRandomInput(number: Int): List<Position> {
        return feelingLayer.getRandomPosition(number)
    }

    fun getRandomOutput(number: Int): List<Position> {
        return feelingLayer.getRandomPosition(number)
    }
}
