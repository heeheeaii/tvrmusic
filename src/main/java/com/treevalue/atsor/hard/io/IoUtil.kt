package com.treevalue.atsor.hard.io

import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import com.treevalue.atsor.hard.TensorManager
import java.io.*
import java.nio.file.Paths

object IoUtil {
    fun <T : Serializable> saveObjectToFile(obj: T, objPath: String) {
        ObjectOutputStream(FileOutputStream(objPath)).use { oos ->
            oos.writeObject(obj)
        }
    }

    fun <T : Serializable> readObjectFromFile(objPath: String): T? {
        return ObjectInputStream(FileInputStream(objPath)).use { ois ->
            try {
                ois.readObject() as T
            } catch (e: Exception) {
                null
            }
        }
    }

    // (height, width, channel)
    fun imgToTensor(imagePath: String): NDArray? {
        val file = File(imagePath)
        if (!file.exists() || !file.isFile) {
            return null
        }
        val image: Image = ImageFactory.getInstance().fromFile(Paths.get(imagePath))
        val manager: NDManager = TensorManager.getManager()
        return image.toNDArray(manager)
    }
}
