package com.treevalue.atsor.data.save

import ai.djl.ndarray.types.DataType
import java.io.*
import java.nio.ByteBuffer

fun <T : Serializable> saveObjAsBin(obj: T, filePath: String) {
    ObjectOutputStream(FileOutputStream(filePath)).use { oos ->
        oos.writeObject(obj)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> loadObjFromBin(filePath: String): T? {
    try {
        ObjectInputStream(FileInputStream(filePath)).use { ois ->
            return ois.readObject() as T
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
data class TensorSaveModel<T>(val shape: MutableList<T>, val data: ByteBuffer, val dataType: DataType) : Serializable
