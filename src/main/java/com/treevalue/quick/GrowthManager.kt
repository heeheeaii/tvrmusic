package com.treevalue.quick

import Pathfinder
import java.util.UUID
import java.util.concurrent.*

/**
 * It orchestrates neuron growth based on A* paths and a clock tick.
 * 外部顶层接口，接收输入层到输出层的生长请求
 */
class GrowthManager private constructor(
    private val pathfinder: Pathfinder,
    tickIntervalMs: Long = 50L,
    private val transform: Transform = Transform.getInstance(),
) {
    companion object {
        @Volatile
        private var instance: GrowthManager? = null

        fun getInstance(): GrowthManager = instance ?: synchronized(this) {
            instance ?: GrowthManager(
                Pathfinder(numLayers = 5, numRow = 32, numCol = 32)
            ).also { instance = it }
        }
    }

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

    fun getInOutMatch(input: List<Position>, output: List<Position>): List<Pair<Int, Int>> {
        val (match, _) = pathfinder.matchPointsByMinCost(input, output)
        return match
    }

    fun requestGrowth(sourceNeuron: Neuron?, targetPosition: Position) {
        if (sourceNeuron == null) {
            return
        }
        val requestKey = sourceNeuron.coordinate to targetPosition

        activeGrowthProcessByHeadTail[requestKey]?.let { existingProcess ->
            activeMiddleGrowthById[existingProcess.id]?.reinforce()
            return
        }

        val startPt = sourceNeuron.coordinate
        val goalPt = targetPosition

        // Find path using A* and convert points to positions
        val path = pathfinder.findPath(startPt, goalPt)?.first
            ?.drop(1) // Drop the starting point as we already have the source neuron
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

    private fun enqueueNextGrowthProcess(process: NeuronsGrowthProcess) {
        if (process.isComplete()) return

        val src = process.getNextPositionStart()
        val tgt = process.getNextPositionTarget()

        val seg = MiddleGrowthEvent(process.id, src, tgt)

        activeMiddleGrowthById[process.id] = seg
        activeMiddleGrowth.offer(seg)
    }
}
