package com.treevalue.atsor.data.nd4j

import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration
import org.nd4j.linalg.api.memory.enums.AllocationPolicy
import org.nd4j.linalg.api.memory.enums.LearningPolicy
import org.nd4j.linalg.api.memory.enums.MirroringPolicy
import org.nd4j.linalg.api.memory.enums.SpillPolicy

class MemoryUtil {
    private val highPerfConfig = WorkspaceConfiguration.builder()
        .initialSize((512 * 1024 * 1024).toLong()) // 512MB 初始
        .maxSize(2L * 1024 * 1024 * 1024) // 最大 2GB
        .overallocationLimit(0.1) // 允许 10% 超分配
        .policyAllocation(AllocationPolicy.OVERALLOCATE)
        .policyLearning(LearningPolicy.FIRST_LOOP)
        .policyMirroring(MirroringPolicy.FULL)
        .policySpill(SpillPolicy.EXTERNAL) // 溢出到磁盘
        .build()
}
