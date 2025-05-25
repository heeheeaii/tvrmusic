package com.treevalue.quick

import com.treevalue.quick.layer.*
import org.nd4j.linalg.api.ndarray.INDArray

class Transform {
    private val eventHandler: EventHandler = EventHandler.getInstance()
    private val feelingLayer: FeelingLayer = FeelingLayer(eventHandler = eventHandler)
    private val shallowLayer: ShallowLayer = ShallowLayer(eventHandler = eventHandler)
    private val deepLayer: DeepLayer = DeepLayer(eventHandler = eventHandler)
    private val outputLayer: OutputLayer = OutputLayer(eventHandler = eventHandler)
    private val feedbackLayer: FeedbackLayer = FeedbackLayer(eventHandler = eventHandler)
    private val layerMap: Map<Int, Layer> = mapOf(
        0 to feelingLayer,
        1 to shallowLayer,
        2 to deepLayer,
        3 to outputLayer,
        4 to feedbackLayer
    )

    init {
        eventHandler.startProcessing()
    }

    fun input(feeling: INDArray) {
        feelingLayer.input(feeling)
    }

    fun connectNeuronTo(sourceNeuron: Neuron, targetPosition: Position): Neuron? {
        val layerIndex = targetPosition.z.toInt()
        return layerMap[layerIndex]?.connectNeuronTo(sourceNeuron, targetPosition)
    }

    fun getNeuron(position: Position): Neuron? {
        val layerIndex = position.z.toInt()
        return layerMap[layerIndex]?.getNeuron(position)
    }

    companion object {
        @Volatile
        private var instance: Transform? = null
        fun getInstance(): Transform = instance ?: synchronized(this) {
            instance ?: Transform().also { instance = it }
        }
    }
}
