package com.treevalue.soundRobot.io

import com.treevalue.soundRobot.hard.io.IoUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.Serializable

data class MyData(val id: Int, val name: String) : Serializable

class IoTest {
    @Test
    fun rwTest() {
        val d = MyData(1, "1")
        val path = "D:\\agi\\tvrmusicnew\\src\\test\\java\\com\\treevalue\\soundRobot\\io\\data\\d1.bin"
        IoUtil.saveObjectToFile(d, path)
        val d2 = IoUtil.readObjectFromFile<MyData>(path)!!
        Assertions.assertEquals(d.name, d2.name)
        Assertions.assertEquals(d.id, d2.id)

    }
}
