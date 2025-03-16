package com.treevalue.atsor.hard

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class MusicMatch(
    @Autowired private val resourceLoader: ResourceLoader,
    @Value("\${tvr.musicRelativePath}") private val musicPath: String
) {

    private val musicUrls = mutableListOf<String>()

    init {
        loadMusicUrl()
    }

    private fun loadMusicUrl() {
        val resource = resourceLoader.getResource("classpath:$musicPath")
        if (resource.exists() && resource.file.isDirectory) {
            val files = resource.file.listFiles()
            files?.forEach { file ->
                if (file.isFile && file.name.endsWith(".mp3")) {
                    musicUrls.add("$musicPath/${file.name}")
                }
            }
        } else {
            println("musicPath error: Path does not exist or is not a directory.")
        }
    }

    fun RandomPath(): String {
        if (musicUrls.isEmpty()) {
            return ""
        }
        val idx = Random.nextInt(0,musicUrls.size)
        return musicUrls[idx]
    }
}
