fun Double.toIntOrNull(): Int? {
    if (this.isNaN() || this.isInfinite()) {
        return null
    }
    if (this < Int.MIN_VALUE.toDouble() || this >= Int.MAX_VALUE.toDouble() + 1.0) {
        return null
    }
    return this.toInt()
}

fun Double.toLongOrNull(): Long? {
    if (this.isNaN() || this.isInfinite()) {
        return null
    }
    if (this < Long.MIN_VALUE.toDouble() || this >= Long.MAX_VALUE.toDouble() + 1.0) {
        return null
    }

    return this.toLong()
}

fun Float.toIntOrNull(): Int? {

    if (this.isNaN() || this.isInfinite()) {
        return null
    }


    if (this < Int.MIN_VALUE.toFloat() || this >= Int.MAX_VALUE.toFloat() + 1.0f) {
        return null
    }

    return this.toInt()
}

fun Float.toLongOrNull(): Long? {
    if (this.isNaN() || this.isInfinite()) {
        return null
    }

    if (this < Long.MIN_VALUE.toFloat() || this >= Long.MAX_VALUE.toFloat() + 1.0f) {
        return null
    }

    return this.toLong()
}
