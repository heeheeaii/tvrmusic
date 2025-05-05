import org.nd4j.linalg.api.buffer.DataType
import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration
import org.nd4j.linalg.api.memory.enums.AllocationPolicy
import org.nd4j.linalg.api.memory.enums.LearningPolicy
import org.nd4j.linalg.api.memory.enums.SpillPolicy
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.ops.transforms.Transforms


object WorkspaceExample {
    @JvmStatic
    fun main(args: Array<String>) {

        // 1. 配置工作空间 (可选，可以使用默认配置)
        val wsConfig: WorkspaceConfiguration = WorkspaceConfiguration.builder()
            .initialSize((100 * 1024 * 1024).toLong()) // 初始大小 100MB
            .maxSize((500 * 1024 * 1024).toLong()) // 最大大小 500MB (如果需要增长)
            .policyAllocation(AllocationPolicy.STRICT) // 分配策略: STRICT (空间不足时报错)
            .policyLearning(LearningPolicy.OVER_TIME) // 学习策略: NONE (不自动调整大小)
            .policySpill(SpillPolicy.FAIL) // 溢出策略: FAIL (空间不足时失败)
            .build()

        // 定义工作空间名称
        val workspaceId = "MyCalculationWorkspace"

        // 2. 使用 try-with-resources 激活并使用工作空间
        try {
            Nd4j.getWorkspaceManager().getAndActivateWorkspace(wsConfig, workspaceId).use { ws ->

                // 在这个 try 块内创建的 INDArray 会使用 'MyCalculationWorkspace' 的内存
                val x: INDArray = Nd4j.rand(DataType.FLOAT, 1000, 1000) // 分配在工作空间内
                val y: INDArray = Nd4j.rand(DataType.FLOAT, 1000, 1000) // 分配在工作空间内
                println("X is allocated in workspace: " + x.isAttached) // 输出 true
                println("Current workspace size: " + ws.currentSize)

                // 计算结果 z 也会分配在当前活动的工作空间内
                val z = x.mmul(y)
                println("Z is allocated in workspace: " + z.isAttached) // 输出 true
                println("Workspace size after mmul: " + ws.currentSize)

                // 在这里可以进行更多的计算...
                val w = z.add(1.0) // w 也在工作空间内
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 在 try 块外部，之前在工作空间中创建的 x, y, z, w 变量引用的是无效内存
        // 尝试访问它们通常会导致错误或未定义行为 (除非使用了 detach/leverage)
        // System.out.println(x); // !!! 危险操作 !!!

        // 如果需要一个不受工作空间影响的数组
        val outsideArray: INDArray = Nd4j.create(DataType.FLOAT, 10, 10) // 在工作空间外部创建，由 GC 管理
        println("Outside array is attached to workspace? " + outsideArray.isAttached) // 输出 false

        // 清理 (如果需要手动管理)
//         Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
    }

    // 从工作空间返回数组
    fun calculationWithResult(input: INDArray): INDArray {
        Nd4j.getWorkspaceManager().getAndActivateWorkspace("ResultWorkspace").use { ws ->
            val tempResult = input.mul(2.0) // 在工作空间内
            val finalResult = tempResult.add(1.0) // 仍在工作空间内

            // 使用 detach() 将数组从工作空间中移出，使其生命周期由 GC 管理
            // detach() 会创建一个新的 INDArray (或只是改变其状态)，其数据复制到常规内存中
            return finalResult.detach()
        }
        // finalResult 在这里是有效的，因为它被 detach 了
    }

    // 在迭代中使用工作空间 (例如 RNN)
    fun iterativeTask(): INDArray {
        var state: INDArray = Nd4j.zeros(DataType.FLOAT, 1, 10) // 初始状态在外部
        val iterWs = "IterativeWS"

        // 配置一个可重用的工作空间
        val iterConfig: WorkspaceConfiguration = WorkspaceConfiguration.builder()
            .initialSize((10 * 1024 * 1024).toLong()) // 10MB
            .policyLearning(LearningPolicy.FIRST_LOOP) // 让它在第一次循环时学习所需大小
            .build()



        for (i in 0..99) {
            Nd4j.getWorkspaceManager().getAndActivateWorkspace(iterConfig, iterWs).use { ws ->
                val input: INDArray = Nd4j.rand(DataType.FLOAT, 1, 10) // 本次迭代的输入，在工作空间内

                // 使用 leverage() 将上一次迭代的结果 (state) "带入" 当前工作空间作用域
                // leverage() 不会复制数据，它假设 state 的数据在下一次循环开始时
                // 仍然位于同一个工作空间内存区域（因为工作空间会被重用）
                // 如果 state 是第一次进入或者来自外部，leverage 可能会触发复制
                val prevState = state.leverage()

                state = Transforms.tanh(prevState.add(input))
            }
        }
        // 循环结束后，最终的 state 仍然指向工作空间内存。
        // 如果需要长期保留它，需要 detach()
        return state.detach()
    }
}
