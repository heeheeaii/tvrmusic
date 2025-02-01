package com.treevalue.soundRobot.hard.io

object CmdUtil {
    fun printProgress(currentStep: Int, totalSteps: Int, progressBarLength: Int = 50) {
        val progress = currentStep.toDouble() / totalSteps
        val filledLength = (progress * progressBarLength).toInt()
        val bar = buildString {
            repeat(filledLength) { append("#") }
            repeat(progressBarLength - filledLength) { append("-") }
        }
        print("\r[ $bar ] ${"%.2f".format(progress * 100)}%")
    }
}
