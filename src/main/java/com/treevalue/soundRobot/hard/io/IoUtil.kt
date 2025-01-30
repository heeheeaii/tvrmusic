package com.treevalue.soundRobot.hard.io

import java.io.*

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
}
