package com.treevalue.soundRobot.hard

class SoundInputer (val mp3path:String): TensorInputer {
    private lateinit var inputor: TensorI
    override fun GetTensor(): TensorI {
        return inputor
    }
}
