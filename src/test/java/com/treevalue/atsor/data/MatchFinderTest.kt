package com.treevalue.atsor.data

import com.treevalue.quick.Point
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MatchFinderTest {

    private val layers = 5
    private val rows = 100
    private val cols = 100
    private val matchfinder = MatchFinder(layers, rows, cols)

    @Test
    fun `test case 1 - straight path`() {
        val start = Point(0, 1, 1)
        val goal = Point(4, 1, 1)
        findPath

        assertNotNull(result, "路径应当存在")
        assertEquals(start, result!!.first.first(), "路径起点应为 start")
        assertEquals(goal, result.first.last(), "路径终点应为 goal")
        matchfinder.printPathResult(result, start, goal)
    }

    @Test
    fun `test case 2 - diagonal path`() {
        val start = Point(0, 0, 0)
        val goal = Point(4, 99, 99)
        val result = matchfinder.findPath(start, goal)

        assertNotNull(result, "路径应当存在")
        assertEquals(start, result!!.first.first())
        assertEquals(goal, result.first.last())
        matchfinder.printPathResult(result, start, goal)
    }

    @Test
    fun `test case 3 - short path`() {
        val start = Point(0, 2, 1)
        val goal = Point(1, 0, 0)
        val result = matchfinder.findPath(start, goal)

        assertNotNull(result)
        assertEquals(start, result!!.first.first())
        assertEquals(goal, result.first.last())
        matchfinder.printPathResult(result, start, goal)
    }

    @Test
    fun `test case 4 - same start and goal`() {
        val start = Point(2, 1, 0)
        val goal = Point(2, 1, 0)
        val result = matchfinder.findPath(start, goal)

        assertNotNull(result)
        assertEquals(0.0, result!!.second)
        assertEquals(listOf(start), result.first)
        matchfinder.printPathResult(result, start, goal)
    }

    @Test
    fun `test case 5 - invalid layer order`() {
        val start = Point(3, 0, 0)
        val goal = Point(1, 2, 2)
        val result = matchfinder.findPath(start, goal)

        assertNull(result, "由于起点层级不小于终点，路径应为 null")
        matchfinder.printPathResult(result, start, goal)
    }

    @Test
    fun `test case 5 - assign connections at the In and out layer`() {
        val inputs = listOf(
            Point(0, 0, 0), Point(0, 30, 70), Point(0, 99, 50)
        )
        val outputs = listOf(
            Point(1, 99, 0),
            Point(1, 20, 80),
            Point(1, 10, 10),
            Point(1, 60, 99)
        )
        val (edges, cost) = matchfinder.matchPointsByMinCost(inputs, outputs, rows, cols)
        println("Edge count = ${edges.size}, total cost = $cost")
        edges.forEach { (i, j) ->
            println("Input[$i] (${inputs[i]})  ->  Output[$j] (${outputs[j]})")
        }
    }
}
