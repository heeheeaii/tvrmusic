import java.util.*
import kotlin.collections.HashMap

class DynamicPriorityQueue<T> {

    private data class Node<T>(
        var value: T,
        var priority: Int,
        var heapIndex: Int = -1, // Index in the heap array
        var listNode: DLLNode<Node<T>>? = null // Reference to the doubly linked list node
    )

    private data class DLLNode<T>(
        var data: T,
        var prev: DLLNode<T>? = null,
        var next: DLLNode<T>? = null
    )

    private val heap = mutableListOf<Node<T>>()
    private val elementMap = HashMap<T, Node<T>>()
    private var insertionOrder: DLLNode<Node<T>>? = null // Head of the doubly linked list
    private var tail: DLLNode<Node<T>>? = null       // Tail of the doubly linked list

    //region  Doubly Linked List Operations
    private fun addToDLL(node: Node<T>) {
        val dllNode = DLLNode<Node<T>>(node)
        node.listNode = dllNode
        if (insertionOrder == null) {
            insertionOrder = dllNode
            tail = dllNode
        } else {
            tail?.next = dllNode
            dllNode.prev = tail
            tail = dllNode
        }
    }

    private fun removeDLLNode(dllNode: DLLNode<Node<T>>) {
        if (dllNode.prev != null) {
            dllNode.prev!!.next = dllNode.next
        } else {
            insertionOrder = dllNode.next
        }
        if (dllNode.next != null) {
            dllNode.next!!.prev = dllNode.prev
        } else {
            tail = dllNode.prev
        }
    }
    //endregion

    //region Heap Operations
    private fun siftUp(index: Int) {
        var current = index
        while (current > 0) {
            val parent = (current - 1) / 2
            if (compareNodes(heap[current], heap[parent]) < 0) {
                swap(current, parent)
                current = parent
            } else {
                break
            }
        }
    }

    private fun siftDown(index: Int) {
        var current = index
        val size = heap.size
        while (true) {
            val leftChild = 2 * current + 1
            val rightChild = 2 * current + 2
            var smallest = current

            if (leftChild < size && compareNodes(heap[leftChild], heap[smallest]) < 0) {
                smallest = leftChild
            }
            if (rightChild < size && compareNodes(heap[rightChild], heap[smallest]) < 0) {
                smallest = rightChild
            }

            if (smallest != current) {
                swap(current, smallest)
                current = smallest
            } else {
                break
            }
        }
    }

    private fun swap(i: Int, j: Int) {
        val temp = heap[i]
        heap[i] = heap[j]
        heap[j] = temp
        heap[i].heapIndex = i
        heap[j].heapIndex = j
    }

    private fun compareNodes(a: Node<T>, b: Node<T>): Int {
        val priorityComparison = a.priority.compareTo(b.priority)
        if (priorityComparison != 0) {
            return priorityComparison
        } else {
            // If priorities are equal, compare based on insertion order (FIFO)
            var nodeA = a.listNode
            var nodeB = b.listNode

            var current = insertionOrder
            while (current != null) {
                if (current == nodeA) {
                    return -1
                }
                if (current == nodeB) {
                    return 1
                }
                current = current.next
            }
            return 0 // Should never reach here if DLL is maintained correctly
        }
    }
    //endregion

    fun add(element: T, priority: Int) {
        if (elementMap.containsKey(element)) {
            changePriority(element, priority) // Or throw exception, depending on desired behavior
            return
        }

        val newNode = Node(element, priority)
        elementMap[element] = newNode
        addToDLL(newNode) //add to linkedlist

        heap.add(newNode)
        newNode.heapIndex = heap.size - 1
        siftUp(newNode.heapIndex)
    }

    fun peek(): T? {
        return heap.firstOrNull()?.value
    }

    fun poll(): T? {
        if (heap.isEmpty()) {
            return null
        }

        val top = heap[0]
        val last = heap.removeAt(heap.size - 1)
        elementMap.remove(top.value)
        removeDLLNode(top.listNode!!) //remove from linkedlist

        if (heap.isNotEmpty()) {
            heap[0] = last
            last.heapIndex = 0
            siftDown(0)
        }

        return top.value
    }

    fun changePriority(element: T, newPriority: Int) {
        val node = elementMap[element] ?: return // Or throw exception if element not found

        val oldPriority = node.priority
        node.priority = newPriority

        if (newPriority < oldPriority) {
            siftUp(node.heapIndex)
        } else if (newPriority > oldPriority) {
            siftDown(node.heapIndex)
        } else {
            //if priority not change, re-sort according to FIFO
            siftDown(node.heapIndex)
            siftUp(node.heapIndex)
        }
    }

    fun remove(element: T): Boolean {
        val nodeToRemove = elementMap[element] ?: return false
        val lastNode = heap.removeAt(heap.size - 1)

        removeDLLNode(nodeToRemove.listNode!!)
        elementMap.remove(element)

        if (nodeToRemove != lastNode) {
            heap[nodeToRemove.heapIndex] = lastNode
            lastNode.heapIndex = nodeToRemove.heapIndex

            val oldPriority = nodeToRemove.priority
            if (lastNode.priority < oldPriority) {
                siftUp(lastNode.heapIndex)
            } else if (lastNode.priority > oldPriority) {
                siftDown(lastNode.heapIndex)
            } else {
                siftDown(lastNode.heapIndex)
                siftUp(lastNode.heapIndex)
            }
        }
        return true
    }

    fun size(): Int {
        return heap.size
    }

    fun isEmpty(): Boolean {
        return heap.isEmpty()
    }

    override fun toString(): String {
        return heap.joinToString(prefix = "[", postfix = "]") { "${it.value}(${it.priority})" }
    }

}
