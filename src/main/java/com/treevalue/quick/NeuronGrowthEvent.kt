package com.treevalue.quick

import AStarPathfinder
import java.util.UUID
import java.util.concurrent.*

/**
 * Represents the growth of a single, continuous segment between two points.
 */
class NeuronGrowthEvent private constructor(
    private val pathfinder: AStarPathfinder,
    tickIntervalMs: Long = 50L,
    private val transform: Transform = Transform.getInstance(),
) {
    companion object {
        @Volatile
        private var instance: NeuronGrowthEvent? = null

        fun getInstance(numLayers: Int = 5, numRows: Int = 32, numCols: Int = 32): NeuronGrowthEvent =
            instance ?: synchronized(this) {
                instance ?: NeuronGrowthEvent(
                    AStarPathfinder(numLayers, numRows, numCols)
                ).also { instance = it }
            }
    }

    private val activeSegments = ConcurrentLinkedQueue<MiddleGrowthEvent>()
    private val pendingNextSegment = ConcurrentLinkedQueue<NeuronsGrowthProcess>()
    private val activePathsById = ConcurrentHashMap<UUID, NeuronsGrowthProcess>()
    private val activePathByRequest = ConcurrentHashMap<Pair<Position, Position>, NeuronsGrowthProcess>()
    private val activeSegmentByProcId = ConcurrentHashMap<UUID, MiddleGrowthEvent>()

    private val growthExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "NeuronGrowthEventThread").apply { isDaemon = true }
        }

    init {
        growthExecutor.scheduleAtFixedRate(::processGrowthTick, 0L, tickIntervalMs, TimeUnit.MILLISECONDS)
    }

    private fun processGrowthTick() {
        var seg = activeSegments.poll()
        while (seg != null) {
            val arrived = seg.advanceThenArrive()

            if (arrived) {
                activeSegmentByProcId.remove(seg.parentProcessId)

                transform.getNeuron(seg.sourcePosition)?.let { src ->
                    transform.connectNeuronTo(src, seg.targetPosition)
                }

                val proc = activePathsById[seg.parentProcessId]
                proc?.advanceToNext()

                if (proc != null && !proc.isComplete()) {
                    pendingNextSegment.offer(proc)
                } else if (proc != null && proc.isComplete()) {
                    val requestKey = proc.initialNeuronPosition to proc.fullPath.last()
                    activePathByRequest.remove(requestKey)
                    activePathsById.remove(proc.id)
                }
            } else {
                activeSegments.offer(seg)
            }
            seg = activeSegments.poll()
        }

        var proc = pendingNextSegment.poll()
        while (proc != null) {
            enqueueNextSegment(proc)
            proc = pendingNextSegment.poll()
        }
    }

    private fun enqueueNextSegment(process: NeuronsGrowthProcess) {
        if (process.isComplete()) return

        val src = process.getNextPositionStart()
        val tgt = process.getNextPositionTarget()
        val seg = MiddleGrowthEvent(process.id, src, tgt)

        activeSegmentByProcId[process.id] = seg
        activeSegments.offer(seg)
    }
}
