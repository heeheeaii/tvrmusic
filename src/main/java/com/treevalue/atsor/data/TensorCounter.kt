package com.treevalue.atsor.data

import java.math.BigInteger
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

class TensorCounter {
    private val counter = AtomicReference(BigInteger.ZERO)
    private val idCache = ConcurrentLinkedQueue<Any>()
    fun getAndAsc(): BigInteger {
        val cachedId = idCache.poll()
        if (cachedId != null) {
            return cachedId as BigInteger
        } else {
            val current = counter.get()
            val next = current.add(BigInteger.ONE)
            while (!counter.compareAndSet(current, next)) {
            }
            return next
        }
    }

    fun dec(id: BigInteger) {
        idCache.offer(id)
    }
}
