import com.treevalue.quick.Memory

class MemoryList : Iterable<Pair<Float, Memory>> {

    private inner class Node(val modelKey: Float, val memory: Memory) {
        var prev: Node? = null
        var next: Node? = null
    }

    private var head: Node? = null
    private var tail: Node? = null
    private val keyMap = HashMap<Float, Node>()

    val size: Int get() = keyMap.size

    fun isEmpty(): Boolean = size == 0

    fun first(): Memory? {
        return head?.memory
    }

    // 添加或提升到头部
    fun putToHead(modelKey: Float, memory: Memory) {
        val node = keyMap[modelKey]
        if (node != null) {
            moveToHead(node)
        } else {
            val newNode = Node(modelKey, memory)
            addToHead(newNode)
            keyMap[modelKey] = newNode
        }
    }

    fun find(modelKey: Float, moveToHead: Boolean = false): Memory? {
        val node = keyMap[modelKey] ?: return null
        if (moveToHead) moveToHead(node)
        return node.memory
    }

    fun remove(modelKey: Float): Boolean {
        val node = keyMap.remove(modelKey) ?: return false
        for (indArray in node.memory.output.values) {
            try {
                (indArray as AutoCloseable).close()
            } catch (e: Exception) {
                // ignore outer close cause error, but should close there
            }
        }
        node.memory.output.clear()
        removeNode(node)
        return true
    }

    fun asList(): List<Memory> {
        val list = mutableListOf<Memory>()
        var node = head
        while (node != null) {
            list.add(node.memory)
            node = node.next
        }
        return list
    }

    private fun addToHead(node: Node) {
        node.next = head
        head?.prev = node
        head = node
        if (tail == null) tail = node
    }

    private fun moveToHead(node: Node) {
        if (node === head) return
        removeNode(node)
        addToHead(node)
    }

    private fun removeNode(node: Node) {
        val prev = node.prev
        val next = node.next
        if (prev != null) prev.next = next else head = next
        if (next != null) next.prev = prev else tail = prev
        node.prev = null
        node.next = null
    }

    override fun iterator(): Iterator<Pair<Float, Memory>> {
        return object : Iterator<Pair<Float, Memory>> {
            var current = head
            override fun hasNext(): Boolean = current != null
            override fun next(): Pair<Float, Memory> {
                val result = current ?: throw NoSuchElementException()
                current = result.next
                return result.modelKey to result.memory
            }
        }
    }
}
