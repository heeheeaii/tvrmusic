package com.treevalue.atsor.lowentrybody.sensoryMachine

import com.treevalue.atsor.TVRMusicApplication
import com.treevalue.atsor.lowentrybody.statemachine.State
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Sensory(private val stateMachine: State) {
    private val logger = LoggerFactory.getLogger(TVRMusicApplication::class.java)
    operator fun get(vararg idxs: Int): Float {
        return stateMachine.get(*idxs)
    }

    fun update(all: Float, use: Float) {
        if (all == 0.0f) updateStateMachine(0.0f) else updateStateMachine(use / all)
    }

    private fun updateStateMachine(stableVal: Float) {
        stateMachine[0] = stableVal
    }
}
