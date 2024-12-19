package com.treevalue.soundRobot.hard

object Machine {
    fun getNumberOfCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}
