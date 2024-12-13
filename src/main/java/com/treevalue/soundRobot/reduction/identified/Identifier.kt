package com.treevalue.soundRobot.reduction.identified

import Sound
import com.treevalue.soundRobot.data.Tensor
import com.treevalue.soundRobot.hard.TensorI
import com.treevalue.soundRobot.hard.TensorInputer

class Identifier {
    private lateinit var inputer: TensorInputer
    fun GetTensorFromOuter(): TensorI {
//        return inputer.GetTensor()
        return TensorI(1,1)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }
}
