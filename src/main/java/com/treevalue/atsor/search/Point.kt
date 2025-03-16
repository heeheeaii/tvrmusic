package com.treevalue.atsor.search

class Point(var info: FloatArray) {
    var spacialSize: Byte = 1
    var connections: MutableList<FloatArray>? = null
    var relations: MutableList<FloatArray>? = null
}
