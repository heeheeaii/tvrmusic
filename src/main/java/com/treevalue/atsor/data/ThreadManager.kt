package com.treevalue.atsor.data

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

open class ThreadManager(context: CoroutineContext) {
    private val coroutineScope = CoroutineScope(context)
    private val activeJobs = ConcurrentHashMap<Int, Job>()
    private val jobIdCounter = AtomicInteger(0)

    fun startThread(block: suspend CoroutineScope.() -> Unit): Int {
        val jobId = jobIdCounter.incrementAndGet()
        val job = coroutineScope.launch {
            try {
                block()
            } finally {
                activeJobs.remove(jobId)
            }
        }
        activeJobs[jobId] = job
        return jobId
    }

    fun stopThread(jobId: Int) {
        activeJobs[jobId]?.cancel()
    }

    fun stopAllThreads() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    fun isThreadRunning(jobId: Int): Boolean {
        return activeJobs[jobId]?.isActive ?: false
    }

    fun getActiveJobCount(): Int {
        return activeJobs.size
    }
}
