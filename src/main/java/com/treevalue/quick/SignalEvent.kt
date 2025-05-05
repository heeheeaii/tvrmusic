package com.treevalue.quick

import org.nd4j.linalg.api.ndarray.INDArray

const val SIGNAL_EXCITATORY_IDX = 0
const val SIGNAL_REWARD_IDX = 1
const val SIGNAL_LENGTH = 5

const val ACTIVE_THRESHOLD = 0.8f

/**
 * Represents a signal transmission event. Uses Neuron object references.
 */
data class SignalEvent(
    val sourceNeuron: Neuron,
    val targetNeuron: Neuron,
    val signal: INDArray,
    val distance: Float,
    val timeSeq: Float
) {
}
