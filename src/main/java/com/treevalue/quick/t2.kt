import kotlin.math.*




data class Point(val x: Int, val y: Int) {
    fun distTo(o: Point) = hypot((x - o.x).toDouble(), (y - o.y).toDouble())
}

/** 检查点坐标是否在 [0, w) × [0, h) 范围内 */
fun checkBounds(pts: List<Point>, w: Int, h: Int, label: String) {
    pts.forEachIndexed { i, p ->
        require(p.x in 0 until w && p.y in 0 until h) {
            "$label[$i] 超出网格范围：$p 不在 0‥${w - 1} × 0‥${h - 1}"
        }
    }
}

// ==============================================================
// 匈牙利算法（O(n³) 方阵版本）
// ==============================================================

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
                if (cur < minv[j]) { minv[j] = cur; way[j] = j0 }
                if (minv[j] < delta) { delta = minv[j]; j1 = j }
            }
            for (j in 0..n) {
                if (used[j]) { u[p[j]] += delta; v[j] -= delta }
                else         { minv[j] -= delta }
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

// ==============================================================
// 最小代价边覆盖
// ==============================================================

/**
 * @return  Pair(边列表, 总代价)
 *         边列表元素为 Pair<输入索引, 输出索引>
 */
fun minCostEdgeCover(
    inputs: List<Point>,
    outputs: List<Point>,
    gridW: Int, gridH: Int
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
    for (i in 0 until m)
        for (j in 0 until n)
            cost[i][j] = inputs[i].distTo(outputs[j])

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
        var bestJ = 0; var bestC = Double.POSITIVE_INFINITY
        for (j in 0 until n)
            if (cost[i][j] < bestC) { bestC = cost[i][j]; bestJ = j }
        edges += i to bestJ
        coveredOut[bestJ] = true
        total += bestC
    }
    // 补足未覆盖输出
    for (j in 0 until n) if (!coveredOut[j]) {
        var bestI = 0; var bestC = Double.POSITIVE_INFINITY
        for (i in 0 until m)
            if (cost[i][j] < bestC) { bestC = cost[i][j]; bestI = i }
        edges += bestI to j
        total += bestC
    }
    return edges to total
}

// ==============================================================
// DEMO
// ==============================================================

fun main() {
    // ----------- 手动指定网格大小 -----------
    val W = 100
    val H = 100

    // 假设随机激活一些点（这里手写几个示例坐标）
    val inputs = listOf(
        Point(0, 0), Point(30, 70), Point(99, 50)
    )
    val outputs = listOf(
        Point(99, 0), Point(20, 80), Point(10, 10), Point(60, 99)
    )

    val (edges, cost) = minCostEdgeCover(inputs, outputs, W, H)
    println("Edge count = ${edges.size}, total cost = $cost")
    edges.forEach { (i, j) ->
        println("Input[$i] (${inputs[i]})  ->  Output[$j] (${outputs[j]})")
    }
}
