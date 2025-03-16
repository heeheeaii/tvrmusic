package com.treevalue.atsor.hard

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object TensorManager : AutoCloseable {
    private val ndManager: NDManager = NDManager.newBaseManager()
    private val tensors: MutableMap<String, NDArray>
    private var nameCounter = BigInteger.ZERO

    init {
        tensors = HashMap()
    }


    fun create(vararg lens: Long): NDArray {
        return create(Shape(lens.toList()))
    }

    fun create(shape: Shape?): NDArray {
        return ndManager.create(shape)
    }

    override fun close() {
        for (itm in tensors) {
            itm.value.close()
        }
        tensors.clear()
        ndManager.close()
    }

    fun add(name: String, tensor: NDArray): NDArray {
        tensors[name] = tensor
        return tensor
    }

    fun close(name: String) {
        val tensor = tensors.remove(name)
        tensor?.close()
    }

    fun create(array: Array<FloatArray>): NDArray {
        return ndManager.create(array)
    }

    fun create(array: FloatArray): NDArray {
        return ndManager.create(array)
    }

    fun create(array: DoubleArray): NDArray {
        return ndManager.create(array)
    }

    fun getManager(): NDManager {
        return ndManager
    }

    fun loadFromBin(filePath: String, shape: Shape): NDArray {
        val byteBuffer = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ).use { channel ->
            val fileSize = channel.size().toInt()
            val buffer = ByteBuffer.allocate(fileSize)
            channel.read(buffer)
            buffer.rewind()
            buffer
        }
        val floatBuffer = byteBuffer.asFloatBuffer()
        val floatArray = FloatArray(floatBuffer.remaining())
        floatBuffer.get(floatArray)
        val tsr = ndManager.create(floatArray, shape)
        tensors[nameCounter.toString()] = tsr
        nameCounter++
        return tsr
    }

    fun loadFromPlain(filePath: String, shape: Shape): NDArray {
        val floatList = mutableListOf<Float>()

        BufferedReader(FileReader(filePath)).use { reader ->
            val line = reader.readLine()
            line.split(",").forEach {
                floatList.add(it.toFloat())
            }
        }

        val floatArray = floatList.toFloatArray()
        val tsr = ndManager.create(floatArray, shape)
        tensors[nameCounter.toString()] = tsr
        nameCounter++
        return tsr
    }

    fun saveBinTo(tensor: NDArray, filePath: String) {
        val byteBuffer = tensor.toByteBuffer()
        FileChannel.open(
            java.nio.file.Paths.get(filePath),
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE
        ).use { channel ->
            while (byteBuffer.hasRemaining()) {
                val bytesWritten = channel.write(byteBuffer)
                if (bytesWritten < 0) {
                    error("Write file failed: Failed to write bytes")
                }
            }
        }
    }

    fun savePlainTo(tensor: NDArray, filePath: String) {
        val floatArray = tensor.toFloatArray()
        BufferedWriter(FileWriter(filePath)).use { writer ->
            floatArray.forEachIndexed { index, value ->
                writer.write(value.toString())
                if (index < floatArray.size - 1) {
                    writer.write(",")
                }
            }
        }
    }
}
