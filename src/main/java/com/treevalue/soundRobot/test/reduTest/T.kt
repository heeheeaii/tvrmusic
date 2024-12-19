import ai.djl.ModelException
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.translate.TranslateException
import com.treevalue.soundRobot.hard.Machine
import com.treevalue.soundRobot.reduction.identified.Identifier
import java.io.IOException

object ImageToTensor {
    @Throws(IOException::class, ModelException::class, TranslateException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val path = "D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\soundRobot\\test\\reduTest\\01612_45.png"
        val manager = NDManager.newBaseManager()
        val tensor: NDArray = ImageUtil.loadImaAsTensor(path, manager)
//        val tensorFiltered = Identifier().identifier(tensor)

        println(Machine.getNumberOfCores())
    }
}
