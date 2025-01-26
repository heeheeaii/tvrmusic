package com.treevalue.soundRobot.search

class PointCloud {
    // relation :  object with foreign object , connection :
    // inner point connection relation
    lateinit var relation: Any
    lateinit var name: String
    lateinit var cloud: ArrayList<Point>
    lateinit var projections: Array<FloatArray>
    fun getProjection() {

    }
}
