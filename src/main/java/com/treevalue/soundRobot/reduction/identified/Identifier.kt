package com.treevalue.soundRobot.reduction.identified

import Sound
import com.treevalue.soundRobot.data.Tensor
import com.treevalue.soundRobot.data.cfg.ConstV
import com.treevalue.soundRobot.hard.SoundInputer
import com.treevalue.soundRobot.hard.TensorI
import com.treevalue.soundRobot.hard.TensorInputer

class Identifier {
    private lateinit var inputer: TensorInputer
    init {
        inputer = SoundInputer(ConstV.MUSIC_PATH)
    }
    fun GetTensorFromOuter(): TensorI {
        return TensorI(1,1)
    }
}
