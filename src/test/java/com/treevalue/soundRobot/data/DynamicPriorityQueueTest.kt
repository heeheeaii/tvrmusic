import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class DynamicPriorityQueueTest {

    private lateinit var queue: DynamicPriorityQueue<String>

    @BeforeEach
    fun setUp() {
        queue = DynamicPriorityQueue()
    }

    @Test
    fun `test basic add and poll`() {
        queue.add("Task1", 3)
        queue.add("Task2", 1)
        queue.add("Task3", 2)
        queue.add("Task4", 1)

        assertEquals("[Task2(1), Task4(1), Task3(2), Task1(3)]", queue.toString())
        assertEquals("Task2", queue.poll())
        assertEquals("Task4", queue.poll())
        assertEquals("[Task3(2), Task1(3)]", queue.toString())
    }

    @Test
    fun `test change priority`() {
        queue.add("Task1", 3)
        queue.add("Task3", 2)
        queue.changePriority("Task1", 0)
        assertEquals("[Task1(0), Task3(2)]", queue.toString())
        assertEquals("Task1", queue.poll())
    }

    @Test
    fun `test remove element`() {
        queue.add("Task3", 2)
        queue.add("Task5", 2)
        queue.add("Task6", 4)
        queue.remove("Task5")
        assertEquals("[Task3(2), Task6(4)]", queue.toString())
    }

    @Test
    fun `test FIFO with same priority`() {
        queue.add("Task3", 2)
        queue.add("Task6", 4)
        queue.add("Task7", 5)
        queue.add("Task8", 5)
        queue.add("Task9", 5)
        queue.changePriority("Task8", 5) //change priority but still 5
        assertEquals("[Task3(2), Task6(4), Task7(5), Task8(5), Task9(5)]", queue.toString())
        assertEquals("Task3", queue.poll())
        assertEquals("Task6", queue.poll())
        assertEquals("Task7", queue.poll())
        assertEquals("Task8", queue.poll())
        assertEquals("Task9", queue.poll())
    }

    @Test
    fun `test empty queue`() {
        assertTrue(queue.isEmpty())
        assertNull(queue.poll())
    }

    @Test
    fun `test remove head`() {
        queue.add("A", 1)
        queue.add("B", 2)
        queue.add("C", 3)
        queue.remove("A")
        assertEquals("[B(2), C(3)]", queue.toString())
    }

    @Test
    fun `test remove tail`() {
        queue.add("B", 2)
        queue.add("C", 3)
        queue.add("D", 4)
        queue.remove("D")
        assertEquals("[B(2), C(3)]", queue.toString())
    }

    @Test
    fun `test remove middle and change priority`() {
        queue.add("B", 2)
        queue.add("E", 2)
        queue.add("F", 3)
        queue.remove("E")
        assertEquals("[B(2), F(3)]", queue.toString())
        queue.changePriority("F", 1)
        assertEquals("[F(1), B(2)]", queue.toString())
    }

    @Test
    fun `test add existing element changes priority`() {
        queue.add("A", 2)
        queue.add("A", 3)
        assertEquals("[A(3)]", queue.toString())
    }

}
