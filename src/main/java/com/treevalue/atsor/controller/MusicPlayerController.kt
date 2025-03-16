package com.treevalue.atsor.controller

import com.treevalue.atsor.TVRMusicApplication
import com.treevalue.atsor.alg.Alg
import com.treevalue.atsor.lowentrybody.sensoryMachine.Sensory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*

@Controller
class MusicPlayerController @Autowired constructor(private val sensory: Sensory, private val alg: Alg) {

    private val logger = LoggerFactory.getLogger(TVRMusicApplication::class.java)

    @GetMapping("/")
    fun musicPlayer(model: Model): String {
        val musicUrl = "/music/Andare - Ludovico Einaudi.mp3"
        model.addAttribute("musicUrl", musicUrl)
        return "index"
    }

    @PostMapping("/previous")
    @ResponseBody
    fun previousTrack(@RequestBody requestBody: Map<String, String>): Map<String, String> {
        updateSensorData(requestBody)
        val musicUrl = alg.getMusicUrl(sensory[0])
        return Collections.singletonMap("musicUrl", musicUrl)
    }

    @PostMapping("/next")
    @ResponseBody
    fun nextTrack(@RequestBody requestBody: Map<String, String>): Map<String, String> {
        updateSensorData(requestBody)
        val musicUrl = alg.getMusicUrl(sensory[0])
        return Collections.singletonMap("musicUrl", musicUrl)
    }

    private fun updateSensorData(requestBody: Map<String, String>) {
        val all = requestBody[Mark.all]?.toFloatOrNull() ?: 0.0f
        val use = requestBody[Mark.use]?.toFloatOrNull() ?: 0.0f
        try {
            sensory.update(all, use)
            logger.info("stable value: " + sensory[0])
        } catch (e: Exception) {
            logger.error("all: $all, use: $use")
        }
    }
}
