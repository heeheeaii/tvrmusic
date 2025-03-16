import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape

class ModuleUtil {
    fun loadModule(path: String): Boolean {
        return false
    }

    fun predict(inTensor: NDArray): NDArray {
        return NDManager.newBaseManager().create(Shape(1, 2))
    }
}
