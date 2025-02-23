package com.treevalue.soundRobot.data.simple

class FixedSizeQueue<T>(private val capacity: Int) {

    private val elements = arrayOfNulls<Any>(capacity)
    private var head = 0
    private var tail = 0
    private var size = 0

    init {
        require(capacity > 0) { "Capacity must be positive" }
    }

    fun enqueue(element: T) {
        if (size == capacity) {
            dequeue()
        }
        elements[tail] = element
        tail = (tail + 1) % capacity
        size++
    }

    fun dequeue(): T? {
        if (isEmpty()) {
            return null
        }
        val element = elements[head] as T
        elements[head] = null
        head = (head + 1) % capacity
        size--
        return element
    }

    fun last(): T? {
        return if (isEmpty()) {
            null
        } else {
            elements[tail] as T
        }
    }
    fun peek(): T? {
        return if (isEmpty()) {
            null
        } else {
            elements[head] as T
        }
    }

    fun get(index: Int): T {
        require(index in 0 until size) { "Index out of bounds" }
        val actualIndex = (head + index) % capacity
        return elements[actualIndex] as T
    }

    fun set(index: Int, element: T) {
        require(index in 0 until size) { "Index out of bounds" }
        val actualIndex = (head + index) % capacity
        elements[actualIndex] = element
    }

    fun isEmpty(): Boolean = size == 0

    fun isFull(): Boolean = size == capacity

    fun size(): Int = size

    fun toList(): List<T> {
        return List(size) { get(it) }
    }

    override fun toString(): String {
        return (0 until size).joinToString(prefix = "[", postfix = "]") {
            val actualIndex = (head + it) % capacity
            elements[actualIndex].toString()
        }
    }
}
