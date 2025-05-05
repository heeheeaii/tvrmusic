package com.treevalue.atsor.data

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

/**
 * 按范围分组键值对的线程安全数据存储。
 *
 * @param V  值类型。
 * @param K  键类型（常见数字类型：Float / Double / Long / Int / Short）。
 * @property rangeSize  组距，必须为正。
 * @property keyClass   键的 KClass，用于运行时类型识别。
 */
class RangedKeyDataStore<K : Any, V>(
    private val rangeSize: K,
    private val keyClass: KClass<K>
) {

    init {
        require(toFloat(rangeSize) > 0.0) { "rangeSize 必须为正数" }
    }

    private val store = ConcurrentHashMap<Long, ConcurrentLinkedQueue<V>>()

    fun put(key: K, value: V) {
        val rangeKey = calculateRangeStartKey(key)
        val queue = store.computeIfAbsent(rangeKey) { ConcurrentLinkedQueue() }
        queue.add(value)
    }

    /** 取出并清空指定键所在区间的全部数据 */
    fun getAllAndClear(key: K): List<V> {
        val rangeKey = calculateRangeStartKey(key)
        val queue = store.remove(rangeKey) ?: return emptyList()
        val list = mutableListOf<V>()
        while (true) {
            val item = queue.poll() ?: break
            list.add(item)
        }
        return list
    }

    /** 查看指定键所在区间的元素数量（只读，不清空） */
    fun getRangeItemCount(key: K): Int {
        val rangeKey = calculateRangeStartKey(key)
        return store[rangeKey]?.size ?: 0
    }

    /** 当前活跃区间数 */
    fun getNumberOfActiveRanges(): Int = store.size

    /** 清空全部数据 */
    fun clearAll() = store.clear()

    /** 把任意支持的 K 转成 Float 用于数学运算 */
    private fun toFloat(k: K): Float = when (k) {
        is Int -> k.toFloat()
        is Long -> k.toFloat()
        is Float -> k
        is Double -> k.toFloat()
        is Short -> k.toFloat()
        else -> throw IllegalArgumentException("不支持的键类型: ${k::class}")
    }

    /**
     * 根据键计算区间起始值（Long），逻辑：
     *   1. 负数时让区间左闭右开（-1..-n）落到正确负区间；
     *   2. 再用区间大小整除并乘回得到起点；
     *   3. 最终结果以 Long 作为 Map 的键。
     */
    private fun calculateRangeStartKey(key: K): Long {
        val step = toFloat(rangeSize)
        var value = toFloat(key)

        if (value < 0 && step > 1) value -= (step - 1)  // 调整负数分段

        return ((value / step).toLong() * step).toLong()
    }

    companion object {

        /**
         * 内联工厂，自动推断 K 的 KClass：
         *
         * ```kotlin
         * val store = RangedKeyDataStore.of<String, Long>(100L)
         * val storeF = RangedKeyDataStore.of<String, Float>(50f)
         * ```
         */
        inline fun <reified K : Any, V> of(rangeSize: K): RangedKeyDataStore<K, V> =
            RangedKeyDataStore(rangeSize, K::class)
    }
}
