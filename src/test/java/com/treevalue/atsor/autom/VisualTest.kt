package com.treevalue.atsor.autom

import VisualReceptor
import org.junit.jupiter.api.Test

class VisualTest {
    @Test
    fun t1() {
        val visualReceptor = VisualReceptor()
        while (visualReceptor.getScreenTensor() == null) {
            Thread.sleep(10)
        }
        visualReceptor.getScreenTensor()?.let {
            println("Tensor shape: ${it.shape}")
        }
        visualReceptor.close()
    }
}
