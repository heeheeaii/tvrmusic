package com.treevalue.atsor.hard

object Machine {
    fun getNumberOfCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}
