package com.treevalue.atsor.data

import kotlin.math.floor

class Numeric(internal val value: Number) : Comparable<Numeric> {

    // Helper for binary operations (plus, minus, times)
    private fun performOperation(
        other: Numeric,
        opDouble: (Double, Double) -> Number,
        opFloat: (Float, Float) -> Number,
        opLong: (Long, Long) -> Number,
        opInt: (Int, Int) -> Number
    ): Numeric {
        val val1 = this.value
        val val2 = other.value

        return when {
            val1 is Double || val2 is Double -> Numeric(opDouble(val1.toDouble(), val2.toDouble()))
            val1 is Float || val2 is Float -> Numeric(opFloat(val1.toFloat(), val2.toFloat()))
            val1 is Long || val2 is Long -> Numeric(opLong(val1.toLong(), val2.toLong()))
            // Int, Short, Byte operations result in Int or Long if promoted
            else -> Numeric(opInt(val1.toInt(), val2.toInt()))
        }
    }

    operator fun plus(other: Numeric): Numeric {
        return performOperation(other,
            { a, b -> a + b },
            { a, b -> a + b },
            { a, b -> a + b },
            { a, b -> a + b }
        )
    }

    operator fun minus(other: Numeric): Numeric {
        return performOperation(other,
            { a, b -> a - b },
            { a, b -> a - b },
            { a, b -> a - b },
            { a, b -> a - b }
        )
    }

    operator fun times(other: Numeric): Numeric {
        return performOperation(other,
            { a, b -> a * b },
            { a, b -> a * b },
            { a, b -> a * b },
            { a, b -> a * b }
        )
    }

    operator fun div(other: Numeric): Numeric {
        val val1 = this.value.toDouble()
        val val2 = other.value.toDouble()
        if (val2 == 0.0) throw ArithmeticException("Division by zero")
        return Numeric(val1 / val2)
    }

    operator fun rem(other: Numeric): Numeric {
        val val1 = this.value
        val val2 = other.value
        // Kotlin's rem operator handles different types appropriately
        return when {
            val1 is Double || val2 is Double -> {
                if (val2.toDouble() == 0.0) throw ArithmeticException("Division by zero")
                Numeric(val1.toDouble().rem(val2.toDouble()))
            }

            val1 is Float || val2 is Float -> {
                if (val2.toFloat() == 0.0f) throw ArithmeticException("Division by zero")
                Numeric(val1.toFloat().rem(val2.toFloat()))
            }

            val1 is Long || val2 is Long -> {
                if (val2.toLong() == 0L) throw ArithmeticException("Division by zero")
                Numeric(val1.toLong().rem(val2.toLong()))
            }

            else -> { // Int, Short, Byte
                if (val2.toInt() == 0) throw ArithmeticException("Division by zero")
                Numeric(val1.toInt().rem(val2.toInt()))
            }
        }
    }

    operator fun unaryMinus(): Numeric {
        return when (val v = this.value) {
            is Double -> Numeric(-v)
            is Float -> Numeric(-v)
            is Long -> Numeric(-v)
            is Int -> Numeric(-v)
            is Short -> Numeric(-v.toInt()) // Promotion to Int for negation
            is Byte -> Numeric(-v.toInt())  // Promotion to Int for negation
            else -> throw IllegalArgumentException("Unsupported type for unary minus: ${v::class}")
        }
    }

    operator fun unaryPlus(): Numeric = this // Unary plus is a no-op

    override operator fun compareTo(other: Numeric): Int {
        val v1 = this.value
        val v2 = other.value

        return when {
            v1 is Double || v2 is Double -> v1.toDouble().compareTo(v2.toDouble())
            v1 is Float || v2 is Float -> v1.toFloat().compareTo(v2.toFloat())
            v1 is Long || v2 is Long -> v1.toLong().compareTo(v2.toLong())
            else -> v1.toInt().compareTo(v2.toInt()) // Int, Short, Byte
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Numeric) return false

        val n1 = this.value
        val n2 = other.value

        // Handle NaN specifically: two NaNs are not considered equal by standard '=='
        if (n1 is Double && n1.isNaN() && n2 is Double && n2.isNaN()) return true // Or false, Python's float('nan') == float('nan') is False. Kotlin's Double.NaN == Double.NaN is False.
        // Let's make them equal for consistency if both are Numeric(NaN)
        if (n1 is Float && n1.isNaN() && n2 is Float && n2.isNaN()) return true

        if ((n1 is Double && n1.isNaN()) || (n2 is Double && n2.isNaN()) ||
            (n1 is Float && n1.isNaN()) || (n2 is Float && n2.isNaN())
        ) return false


        // Promote to the "highest" precision type for comparison for equality
        return when {
            n1 is Double || n2 is Double -> n1.toDouble() == n2.toDouble()
            n1 is Float || n2 is Float -> n1.toFloat() == n2.toFloat()
            n1 is Long || n2 is Long -> n1.toLong() == n2.toLong()
            else -> n1.toInt() == n2.toInt() // Int, Short, Byte
        }
    }

    override fun hashCode(): Int {
        val v = this.value

        // Strategy: if it's an exact integer value, hash its Long representation.
        // Otherwise, hash its Double representation. This helps Numeric(1) and Numeric(1.0) have same hash.
        return when (v) {
            is Double -> {
                if (v.isNaN()) return Double.NaN.toBits().toInt() // Consistent hash for NaN
                // Check if it's a whole number and can be represented as Long without loss
                if (v == floor(v) && !v.isInfinite()) {
                    val asLong = v.toLong()
                    // Ensure that converting to Long and back to Double preserves the value
                    if (asLong.toDouble() == v) return asLong.hashCode()
                }
                v.hashCode()
            }

            is Float -> {
                if (v.isNaN()) return Float.NaN.toBits() // Consistent hash for NaN
                val dVal = v.toDouble() // Promote to double for consistent handling
                if (dVal == floor(dVal) && !dVal.isInfinite()) {
                    val asLong = dVal.toLong()
                    if (asLong.toDouble() == dVal) return asLong.hashCode()
                }
                dVal.hashCode()
            }

            is Long -> v.hashCode()
            is Int -> v.toLong().hashCode()
            is Short -> v.toLong().hashCode()
            is Byte -> v.toLong().hashCode()
            else -> v.toDouble().hashCode()
        }
    }

    override fun toString(): String {
        return value.toString()
    }

    // You can add more math functions here if needed e.g. pow, sqrt
    // fun pow(exponent: Numeric): Numeric { ... }
}

fun Number.toNumeric(): Numeric = Numeric(this)
