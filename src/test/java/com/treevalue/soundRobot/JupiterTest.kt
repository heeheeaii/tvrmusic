package com.treevalue.soundRobot

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

class JupiterTest {
    @Nested
    inner class AdditionTests {

        @Test
        fun testAddition() {
            println("neste")
            assertEquals(3, 1 + 2)
        }

        @Test
        @Tag("slow")
        @DisplayName("Testing addition of two numbers")
        fun testAdditionWithNegative() {
            println("test add")
            assertEquals(0, -1 + 1)
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun initAll() {
            println("This is executed before all tests.")
        }
    }

    @Test
    fun testAddition() {
        println("test outer")
        assertEquals(2, 1 + 1)
    }
}
