package com.treevalue.atsor.data.simple

import java.util.concurrent.PriorityBlockingQueue

data class Element<T>(val priority: Int, val value: T) : Comparable<Element<T>> {
    override fun compareTo(other: Element<T>): Int {
        return this.priority.compareTo(other.priority)
    }
}

class PriorityQueue<T> : PriorityBlockingQueue<Element<T>>() {
    fun push(priority: Int, element: T) {
        put(Element(priority, element))
    }
}
