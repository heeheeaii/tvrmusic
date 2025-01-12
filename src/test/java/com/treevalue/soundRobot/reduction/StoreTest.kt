
import java.io.Serializable


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val storage: ObjectStorage = FileObjectStorage()

        var map = mutableMapOf(
            "k1" to "v1",
            "k2" to "v1",
            "k3" to "v1",
            "k4" to "v1",
        )
        val person = Person("Alice", 30)
        storage.store(map, "D:\\agi\\tvrmusicnew\\src\\test\\java\\com\\treevalue\\soundRobot\\tmp\\person.dat")

        val loadedPerson = storage.load("D:\\agi\\tvrmusicnew\\src\\test\\java\\com\\treevalue\\soundRobot\\tmp\\person.dat",MutableMap::class.java)
        if (loadedPerson != null) {
            println("Loaded Person: $loadedPerson")
        } else {
            println("Failed to load person.")
        }



        val loadedMessage = storage.load("message.dat", String::class.java)
        if (loadedMessage != null) {
            println("Loaded Message: $loadedMessage")
        } else {
            println("Failed to load message.")
        }
    }
}

internal class Person(var name: String, var age: Int) : Serializable {
    override fun toString(): String {
        return "Person{name='$name', age=$age}"
    }
}
