import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.Serializable

class ObjectStorageTest {

    private val storage: ObjectStorage = FileObjectStorage()

    @Test
    fun `test store and load person`() {
        // Arrange
        val map = mutableMapOf(
            "k1" to "v1",
            "k2" to "v1",
            "k3" to "v1",
            "k4" to "v1"
        )

        val person = Person("Alice", 30)
        val filePath = "D:\\agi\\tvrmusicnew\\src\\test\\java\\com\\treevalue\\soundRobot\\tmp\\person.dat"

        // Act
        storage.store(map, filePath)
        val loadedPerson = storage.load(filePath, MutableMap::class.java)

        // Assert
        assertNotNull(loadedPerson, "Loaded person should not be null.")
        if (loadedPerson != null) {
            assertTrue(loadedPerson is Map<*, *>, "Loaded object should be a Map.")
            val loadedMap = loadedPerson as Map<*, *>
            assertEquals(4, loadedMap.size, "Map should have 4 entries.")
        }
    }

    @Test
    fun `test load non-existent file`() {
        // Act
        val loadedMessage = storage.load("message.dat", String::class.java)

        // Assert
        assertNull(loadedMessage, "Loaded message should be null since the file does not exist.")
    }
}

internal class Person(var name: String, var age: Int) : Serializable {
    override fun toString(): String {
        return "Person{name='$name', age=$age}"
    }
}
