package com.treevalue.atsor.data

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NumericTest {

    @Test
    fun `test construction and toString`() {
        assertEquals("10", Numeric(10).toString())
        assertEquals("3.0", Numeric(3.0).toString())
        assertEquals("2.5", Numeric(2.5f).toString())
        assertEquals("4", Numeric(4L).toString())
    }

    @Test
    fun `test addition and type promotion`() {
        assertEquals(Numeric(13.0), Numeric(10) + Numeric(3.0))
        assertEquals(Numeric(6L), Numeric(10) - Numeric(4L))
        assertEquals(Numeric(7.5), Numeric(3.0) * Numeric(2.5f))
    }

    @Test
    fun `test division and remainder`() {
        assertEquals(Numeric(3.3333333333333335), Numeric(10) / Numeric(3.0))
        assertEquals(Numeric(2.5), Numeric(5) / Numeric(2))
        assertEquals(Numeric(1.0), Numeric(10) % Numeric(3.0))
        assertEquals(Numeric(1), Numeric(7) % Numeric(3))
    }

    @Test
    fun `test division by zero throws`() {
        assertThrows(ArithmeticException::class.java) {
            Numeric(5) / Numeric(0)
        }
        assertThrows(ArithmeticException::class.java) {
            Numeric(5.0) / Numeric(0.0)
        }
        assertThrows(ArithmeticException::class.java) {
            Numeric(5f) / Numeric(0f)
        }
    }

    @Test
    fun `test remainder by zero throws`() {
        assertThrows(ArithmeticException::class.java) {
            Numeric(5) % Numeric(0)
        }
        assertThrows(ArithmeticException::class.java) {
            Numeric(5.0) % Numeric(0.0)
        }
        assertThrows(ArithmeticException::class.java) {
            Numeric(5f) % Numeric(0f)
        }
    }

    @Test
    fun `test unary minus and plus`() {
        assertEquals(Numeric(-10), -Numeric(10))
        assertEquals(Numeric(-4L), -Numeric(4L))
        assertEquals(Numeric(-3.0), -Numeric(3.0))
        assertEquals(Numeric(-2.5f), -Numeric(2.5f))
        val a = Numeric(6)
        assertEquals(a, +a)
    }

    @Test
    fun `test comparisons`() {
        assertTrue(Numeric(10) > Numeric(3.0))
        assertFalse(Numeric(4L) <= Numeric(2.5f))
        assertEquals(0, Numeric(2.0).compareTo(Numeric(2f)))
        assertTrue(Numeric(5) < Numeric(7L))
        assertTrue(Numeric(3.14) > Numeric(3))
        assertTrue(Numeric(3.0f) < Numeric(3.1))
    }

    @Test
    fun `test equality and hashCode consistency`() {
        val a = Numeric(1)
        val b = Numeric(1.0)
        val c = Numeric(1L)
        val d = Numeric(1.0f)
        assertEquals(a, b)
        assertEquals(a, c)
        assertEquals(a, d)
        assertEquals(b, c)
        assertEquals(b, d)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a.hashCode(), c.hashCode())
        assertEquals(a.hashCode(), d.hashCode())
    }

    @Test
    fun `test list sorting works and is consistent`() {
        val list = listOf(Numeric(5), Numeric(2.0), Numeric(8), Numeric(0.5f))
        val sorted = list.sorted()
        val expected = listOf(Numeric(0.5f), Numeric(2.0), Numeric(5), Numeric(8))
        assertEquals(expected, sorted)
    }

    @Test
    fun `test NaN behavior and hashCode`() {
        val nanDouble = Numeric(Double.NaN)
        val nanFloat = Numeric(Float.NaN)
        assertEquals(nanDouble, Numeric(Double.NaN))
        assertEquals(nanFloat, Numeric(Float.NaN))
        assertNotEquals(nanDouble, Numeric(1.0))
        assertEquals(nanDouble.hashCode(), Double.NaN.toBits().toInt())
        assertEquals(nanFloat.hashCode(), Float.NaN.toBits())
    }

    @Test
    fun `test equals for mixed NaN and number`() {
        assertFalse(Numeric(Double.NaN) == Numeric(1.0))
        assertFalse(Numeric(Float.NaN) == Numeric(1.0f))
    }

    @Test
    fun `test extension function toNumeric`() {
        assertEquals(Numeric(42), 42.toNumeric())
        assertEquals(Numeric(2.5), 2.5.toNumeric())
        assertEquals(Numeric(9L), 9L.toNumeric())
        assertEquals(Numeric(7.7f), 7.7f.toNumeric())
    }
}
