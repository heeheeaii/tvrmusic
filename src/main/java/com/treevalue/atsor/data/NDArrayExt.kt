package com.treevalue.atsor.data

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import java.io.*
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

internal data class NDArrayStore(val inData: ByteArray, val type: DataType, val shape: MutableList<Long>) :
    Serializable

fun NDArray.save(path: String) {
    FileOutputStream(path).use { fileOut ->
        GZIPOutputStream(fileOut).use { gzipOut ->
            ObjectOutputStream(gzipOut).use { objectOut ->
                val store = NDArrayStore(toByteArray(), this.dataType, this.shape.shape.toMutableList())
                objectOut.writeObject(store)
            }
        }
    }
}

fun NDManager.load(path: String): NDArray? {
    FileInputStream(path).use { fileIn ->
        GZIPInputStream(fileIn).use { gzipIn ->
            ObjectInputStream(gzipIn).use { objectIn ->
                val obj = objectIn.readObject()
                return if (obj is NDArrayStore) {
                    create(ByteBuffer.wrap(obj.inData), Shape(obj.shape), obj.type)
                } else {
                    null
                }
            }
        }
    }
}
