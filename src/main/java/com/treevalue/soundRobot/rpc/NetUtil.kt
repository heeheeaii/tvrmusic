package com.treevalue.soundRobot.rpc

import ai.djl.ndarray.NDArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.Callable

@Controller
class NetUtil {
    @Value("\${py.uri}")
    private lateinit var pyUri: String

    companion object {
        private val client: WebClient = WebClient.builder().baseUrl("").build()
        fun <T> send(method: String, tensor: NDArray, callable: Callable<T>) {
            val rst = client.post().uri(method).bodyValue(tensor).retrieve().bodyToMono(String.javaClass)
        }
    }

    @PostMapping("/receive_tensor")
    fun receiveTensor(@RequestBody tensor: NetTensor) {
    }
}
