import ai.djl.ModelException
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.translate.TranslateException
import com.treevalue.soundRobot.data.load
import com.treevalue.soundRobot.data.save
import java.io.IOException

object ImageToTensor {
    @Throws(IOException::class, ModelException::class, TranslateException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val path = "D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\soundRobot\\test\\reduTest\\01612_45.png"
        val manager = NDManager.newBaseManager()
        val tensor: NDArray = ImageConverter.loadImaAsTensor(path, manager)
        println(tensor.shape)
        tensor.save("D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\soundRobot\\test\\reduTest\\t.bin")
        val t = manager.load("D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\soundRobot\\test\\reduTest\\t.bin")!!
        println(t.shape)
    }
}
