package com.treevalue.quick

import AStarPathfinder
import java.util.UUID
import java.util.concurrent.*

/**
 * CORRECTED: This is the main growth management class, renamed from NeuronGrowthEvent.
 * It orchestrates neuron growth based on A* paths and a clock tick.
 */
class GrowthManager private constructor(
    private val pathfinder: AStarPathfinder,
    tickIntervalMs: Long = 50L,
    private val transform: Transform = Transform.getInstance(),
) {
    companion object {
        @Volatile
        private var instance: GrowthManager? = null

        fun getInstance(): GrowthManager = instance ?: synchronized(this) {
            instance ?: GrowthManager(
                AStarPathfinder(numLayers = 5, numRows = 32, numCols = 32)
            ).also { instance = it }
        }
    }

    // CORRECTION: State collections now use the correct types and names.
    private val activeMiddleGrowth = ConcurrentLinkedQueue<MiddleGrowthEvent>()
    private val pendingNextGrowthProcess = ConcurrentLinkedQueue<NeuronsGrowthProcess>()
    private val activeGrowthProcessById = ConcurrentHashMap<UUID, NeuronsGrowthProcess>()
    private val activeGrowthProcessByHeadTail = ConcurrentHashMap<Pair<Position, Position>, NeuronsGrowthProcess>()
    private val activeMiddleGrowthById = ConcurrentHashMap<UUID, MiddleGrowthEvent>()

    private val growthExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "GrowthManagerThread").apply { isDaemon = true }
        }

    init {
        growthExecutor.scheduleAtFixedRate(::processGrowthTick, 0L, tickIntervalMs, TimeUnit.MILLISECONDS)
    }

    fun requestGrowth(sourceNeuron: Neuron, targetPosition: Position) {
        val requestKey = sourceNeuron.coordinate to targetPosition

        activeGrowthProcessByHeadTail[requestKey]?.let { existingProcess ->
            activeMiddleGrowthById[existingProcess.id]?.reinforce()
            return
        }

        val startPt = positionToPoint(sourceNeuron.coordinate)
        val goalPt = positionToPoint(targetPosition)

        // Find path using A* and convert points to positions
        val path = pathfinder.findPath(startPt, goalPt)?.first
            ?.drop(1) // Drop the starting point as we already have the source neuron
            ?.map(::pointToPosition)
            ?: return

        if (path.isEmpty()) return

        val newProcess = NeuronsGrowthProcess(fullPath = path, initialNeuronPosition = sourceNeuron.coordinate)
        activeGrowthProcessById[newProcess.id] = newProcess
        activeGrowthProcessByHeadTail[requestKey] = newProcess

        enqueueNextGrowthProcess(newProcess)
    }

    fun stop() {
        growthExecutor.shutdownNow()
        activeMiddleGrowth.clear()
        pendingNextGrowthProcess.clear()
        activeGrowthProcessById.clear()
        activeGrowthProcessByHeadTail.clear()
        activeMiddleGrowthById.clear()
    }

    private fun processGrowthTick() {
        val numberToProcess = activeMiddleGrowth.size
        for (i in 0 until numberToProcess) {
            val seg = activeMiddleGrowth.poll() ?: continue

            if (seg.advanceThenArrive()) {
                activeMiddleGrowthById.remove(seg.parentProcessId)

                transform.getNeuron(seg.sourcePosition)?.let { src ->
                    transform.connectNeuronTo(src, seg.targetPosition)
                }

                val proc = activeGrowthProcessById[seg.parentProcessId]
                proc?.advanceToNext()

                if (proc != null) {
                    if (!proc.isComplete()) {
                        pendingNextGrowthProcess.offer(proc)
                    } else {
                        val reqHeadTail = proc.initialNeuronPosition to proc.fullPath.last()
                        activeGrowthProcessByHeadTail.remove(reqHeadTail)
                        activeGrowthProcessById.remove(proc.id)
                    }
                }
            } else {
                activeMiddleGrowth.offer(seg)
            }
        }

        // Enqueue the next segments for all paths that have advanced.
        while (pendingNextGrowthProcess.isNotEmpty()) {
            val proc = pendingNextGrowthProcess.poll()
            if (proc != null) enqueueNextGrowthProcess(proc)
        }
    }

    private fun positionToPoint(pos: Position): AStarPathfinder.Point =
        AStarPathfinder.Point(
            layer = pos.z.toInt(),
            row = pos.y.toInt(),
            col = pos.x.toInt()
        )

    private fun pointToPosition(pt: AStarPathfinder.Point): Position =
        Position(x = pt.col.toFloat(), y = pt.row.toFloat(), z = pt.layer.toFloat())

    private fun enqueueNextGrowthProcess(process: NeuronsGrowthProcess) {
        if (process.isComplete()) return

        val src = process.getNextPositionStart()
        val tgt = process.getNextPositionTarget()

        val seg = MiddleGrowthEvent(process.id, src, tgt)

        activeMiddleGrowthById[process.id] = seg
        activeMiddleGrowth.offer(seg)
    }
}
