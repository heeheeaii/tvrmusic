package com.treevalue.atsor.inference

import ai.djl.ndarray.NDArray
import com.treevalue.atsor.data.TensorContainer
import java.util.LinkedList

class Attention {
    private val attention = TensorContainer(2)

    class Reference {
        private val referenceLinks = LinkedList<LinkedList<NDArray>>()

    }

    class TaskPool {
//        private
    }
}
