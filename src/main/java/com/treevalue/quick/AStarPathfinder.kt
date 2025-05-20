import java.util.PriorityQueue
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

class AStarPathfinder( // A* 算法 Admissible Optimal
    private val numLayers: Int, // 总层数
    private val numRows: Int,   // 每层的行数
    private val numCols: Int    // 每层的列数
) {
    data class Point(val layer: Int, val row: Int, val col: Int) {
        fun distTo(o: Point): Double = hypot(
            hypot((layer - o.layer).toDouble(), (row - o.row).toDouble()),
            (col - o.col).toDouble()
        )

        override fun toString() = "P(L$layer, R$row, C$col)"
    }

    /** 检查点坐标是否在 [0, w) × [0, h) 范围内 */
    private fun checkBounds(pts: List<Point>, w: Int, h: Int, label: String) {
        pts.forEachIndexed { i, p ->
            require(p.row in 0 until w && p.col in 0 until h) {
                "$label[$i] 超出网格范围：$p 不在 0‥${w - 1} × 0‥${h - 1}"
            }
        }
    }

    // 匈牙利算法（O(n³) 方阵版本）
    private fun hungarian(cost: Array<DoubleArray>): IntArray {
        val n = cost.size
        val u = DoubleArray(n + 1)
        val v = DoubleArray(n + 1)
        val p = IntArray(n + 1)
        val way = IntArray(n + 1)

        for (i in 1..n) {
            p[0] = i
            var j0 = 0
            val minv = DoubleArray(n + 1) { Double.POSITIVE_INFINITY }
            val used = BooleanArray(n + 1)
            do {
                used[j0] = true
                val i0 = p[j0]
                var delta = Double.POSITIVE_INFINITY
                var j1 = 0
                for (j in 1..n) if (!used[j]) {
                    val cur = cost[i0 - 1][j - 1] - u[i0] - v[j]
                    if (cur < minv[j]) {
                        minv[j] = cur; way[j] = j0
                    }
                    if (minv[j] < delta) {
                        delta = minv[j]; j1 = j
                    }
                }
                for (j in 0..n) {
                    if (used[j]) {
                        u[p[j]] += delta; v[j] -= delta
                    } else {
                        minv[j] -= delta
                    }
                }
                j0 = j1
            } while (p[j0] != 0)

            do {
                val j1 = way[j0]
                p[j0] = p[j1]
                j0 = j1
            } while (j0 != 0)
        }

        val match = IntArray(n) { -1 }          // 行 → 列
        for (j in 1..n) if (p[j] != 0) match[p[j] - 1] = j - 1
        return match
    }

    /**
     * @return  Pair(边列表, 总代价)
     *         边列表元素为 Pair<输入索引, 输出索引>
     */
    fun minCostEdgeCover(
        inputs: List<Point>, outputs: List<Point>, gridW: Int, gridH: Int
    ): Pair<List<Pair<Int, Int>>, Double> {

        // ----------- 边界检查 -----------
        checkBounds(inputs, gridW, gridH, "Input")
        checkBounds(outputs, gridW, gridH, "Output")

        val m = inputs.size
        val n = outputs.size
        val size = max(m, n)            // 方阵维度
        val BIG = 1e9                   // 虚点代价

        // ----------- 构造 padded 成本矩阵 -----------
        val cost = Array(size) { DoubleArray(size) { BIG } }
        for (i in 0 until m) for (j in 0 until n) cost[i][j] = inputs[i].distTo(outputs[j])

        // ----------- 匹配 + 生成最小边覆盖 -----------
        val match = hungarian(cost)
        val edges = mutableListOf<Pair<Int, Int>>()
        val coveredIn = BooleanArray(m)
        val coveredOut = BooleanArray(n)
        var total = 0.0

        // 已匹配的边
        for (i in 0 until m) {
            val j = match[i]
            if (j in 0 until n) {
                edges += i to j
                coveredIn[i] = true
                coveredOut[j] = true
                total += cost[i][j]
            }
        }
        // 补足未覆盖输入
        for (i in 0 until m) if (!coveredIn[i]) {
            var bestJ = 0;
            var bestC = Double.POSITIVE_INFINITY
            for (j in 0 until n) if (cost[i][j] < bestC) {
                bestC = cost[i][j]; bestJ = j
            }
            edges += i to bestJ
            coveredOut[bestJ] = true
            total += bestC
        }
        // 补足未覆盖输出
        for (j in 0 until n) if (!coveredOut[j]) {
            var bestI = 0;
            var bestC = Double.POSITIVE_INFINITY
            for (i in 0 until m) if (cost[i][j] < bestC) {
                bestC = cost[i][j]; bestI = i
            }
            edges += bestI to j
            total += bestC
        }
        return edges to total
    }

    /**
     * 启发函数 (h)：估计从点 p1 到点 p2 的成本 (三维欧几里得距离)
     */
    private fun heuristic(p1: Point, p2: Point): Double {
        val dLayer = (p1.layer - p2.layer).toDouble()
        val dRow = (p1.row - p2.row).toDouble()
        val dCol = (p1.col - p2.col).toDouble()
        return sqrt(dLayer * dLayer + dRow * dRow + dCol * dCol)
    }

    /**
     * 代价函数 (cost)：计算从 p1 到相邻下一层 p2 的实际连接成本
     */
    private fun cost(p1: Point, p2: Point): Double {
        // 确保 p1 和 p2 在相邻层
        if (p2.layer - p1.layer != 1) {
            return Double.POSITIVE_INFINITY // 不应该发生，因为 getNeighbors 只会生成下一层的邻居
        }
        val dRow = (p1.row - p2.row).toDouble()
        val dCol = (p1.col - p2.col).toDouble()
        return sqrt(1.0 + dRow * dRow + dCol * dCol) // (p2.layer - p1.layer)^2 == 1^2 == 1.0
    }

    /**
     * 获取一个点的所有有效邻居 (即下一层的所有点)
     */
    private fun getNeighbors(point: Point): List<Point> {
        val neighbors = mutableListOf<Point>()
        if (point.layer < numLayers - 1) { // 如果不是最后一层
            val nextLayer = point.layer + 1
            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    neighbors.add(Point(nextLayer, r, c))
                }
            }
        }
        return neighbors
    }

    /**
     * 从 cameFrom 映射中重建路径
     */
    private fun reconstructPath(cameFrom: Map<Point, Point>, current: Point): List<Point> {
        val totalPath = mutableListOf(current)
        var tempCurrent = current
        while (cameFrom.containsKey(tempCurrent)) {
            tempCurrent = cameFrom[tempCurrent]!!
            totalPath.add(0, tempCurrent) // 添加到路径的开头
        }
        return totalPath
    }

    /**
     * A* 寻路算法主体
     * @param start 起点
     * @param goal 目标点
     * @return Pair(路径列表, 总成本)? 如果找到路径则返回路径和成本，否则返回 null
     */
    fun findPath(start: Point, goal: Point): Pair<List<Point>, Double>? {
        // 基本校验
        if (start == goal) {
            return Pair(listOf(start), 0.0)
        }
        if (start.layer >= numLayers || goal.layer >= numLayers || start.row >= numRows || goal.row >= numRows || start.col >= numCols || goal.col >= numCols || start.layer < 0 || goal.layer < 0 || start.row < 0 || goal.row < 0 || start.col < 0 || goal.col < 0) {
            println("错误：起点或终点坐标超出边界。")
            return null
        }
        if (start.layer >= goal.layer) {
            println("错误：起点层级必须小于终点层级。($start -> $goal)")
            return null
        }

        val closedSet = mutableSetOf<Point>() // 已评估过的节点集合
        val cameFrom = mutableMapOf<Point, Point>() // 记录路径

        // gScore: 从起点到某点的实际最低成本
        val gScore = mutableMapOf<Point, Double>().withDefault { Double.POSITIVE_INFINITY }
        gScore[start] = 0.0

        // fScore: gScore + heuristic (预估总成本)
        // PriorityQueue 按 fScore 对节点排序
        val fScore = mutableMapOf<Point, Double>().withDefault { Double.POSITIVE_INFINITY }
        fScore[start] = heuristic(start, goal)

        val openSet = PriorityQueue<Point>(compareBy { fScore.getValue(it) })
        openSet.add(start)

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()!! // 取出 fScore 最低的节点

            if (current == goal) {
                val path = reconstructPath(cameFrom, current)
                return Pair(path, gScore.getValue(goal)) // 找到路径，返回路径和成本
            }

            closedSet.add(current)

            for (neighbor in getNeighbors(current)) {
                if (neighbor in closedSet) {
                    continue // 忽略已评估过的邻居
                }

                // 计算通过 current 到达 neighbor 的 gScore
                val tentativeGScore = gScore.getValue(current) + cost(current, neighbor)

                if (tentativeGScore < gScore.getValue(neighbor)) {
                    // 这条路径更优
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeGScore
                    fScore[neighbor] = tentativeGScore + heuristic(neighbor, goal)

                    if (neighbor !in openSet) {
                        openSet.add(neighbor)
                    } else {
                        // 如果邻居已在 openSet 中，需要更新其优先级
                        // 标准 PriorityQueue 没有高效的 decrease-key 操作
                        // 先移除再添加可以达到效果 (对于小图，性能影响可接受)
                        openSet.remove(neighbor) // O(N)
                        openSet.add(neighbor)    // O(log N)
                    }
                }
            }
        }
        return null // 未找到路径
    }

    fun printPathResult(
        result: Pair<List<AStarPathfinder.Point>, Double>?, start: AStarPathfinder.Point, goal: AStarPathfinder.Point
    ) {
        if (result != null) {
            val (path, cost) = result
            println("路径已找到 (从 $start 到 $goal):")
            path.forEachIndexed { index, point ->
                print("  $point")
                if (index < path.size - 1) print(" ->")
                println()
            }
            println("总欧几里得距离 (成本): ${String.format("%.4f", cost)}")
        } else {
            println("未能找到从 $start 到 $goal 的路径。")
        }
    }
}
