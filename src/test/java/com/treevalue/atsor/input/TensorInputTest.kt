package com.treevalue.atsor.input

import ai.djl.engine.Engine
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.index.NDIndex
import com.treevalue.atsor.hard.TensorAudio


class TensorInputTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var t1 = System.currentTimeMillis()
            var a = TensorAudio().audioToTensor("D:\\agi\\tvrmusicnew\\src\\main\\resources\\static\\music\\wav\\tra.wav")
            var line2= a.shape.get(1)
            for (idx in 0 until line2) {
                var tmp = a.get(NDIndex(":,$idx,:"))
                var tmpShap = tmp.shape
            }
            var c = a.get(NDIndex("1:2:3"))
            NDManager.newBaseManager().use { manager ->
                val x = manager.create(2.0f)
                x.setRequiresGradient(true)
                Engine.getInstance().newGradientCollector().use { gc ->
                    val y = x.pow(2)
                    gc.backward(y)
                }
                val xGrad: NDArray = x.getGradient()

                System.out.println(xGrad.toFloatArray().get(0))
                val t2 = System.currentTimeMillis()
                println(t2 - t1)
            }
        }
    }
}
