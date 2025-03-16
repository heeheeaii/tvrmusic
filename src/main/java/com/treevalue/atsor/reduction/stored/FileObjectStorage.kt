import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths

class FileObjectStorage : ObjectStorage {
    override fun <T> store(obj: T, path: String) {
        val filePath = Paths.get(path)
        try {
            ObjectOutputStream(Files.newOutputStream(filePath)).use { oos -> oos.writeObject(obj) }
        } catch (e: IOException) {
            System.err.println("Error storing object to file: " + e.message)
        }
    }

    override fun <T> load(path: String, clazz: Class<T>): T? {
        val filePath = Paths.get(path)
        if (!Files.exists(filePath)) {
            return null
        }
        try {
            ObjectInputStream(Files.newInputStream(filePath)).use { ois ->
                val obj = ois.readObject()
                return clazz.cast(obj)
            }
        } catch (e: IOException) {
            System.err.println("Error loading object from file: " + e.message)
            return null
        } catch (e: ClassNotFoundException) {
            System.err.println("Error loading object from file: " + e.message)
            return null
        } catch (e: ClassCastException) {
            System.err.println("Error loading object from file: " + e.message)
            return null
        }
    }
}
