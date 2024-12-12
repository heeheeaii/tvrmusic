package com.treevalue.soundRobot.data

class TensorPoint {
    var coordinate: MutableList<Int>? = null
    var inBodyOf: Long = 0
    var next: TensorPoint? = null
    var preview: TensorPoint? = null
    var value: Int = 0
}
