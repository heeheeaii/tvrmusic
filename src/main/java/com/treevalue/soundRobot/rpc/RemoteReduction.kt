package com.treevalue.soundRobot.rpc

import ai.djl.ndarray.NDArray
import org.nd4j.serde.jackson.shaded.NDArrayTextDeSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.Callable

@Service
class RemoteReduction {
    companion object {
        @Value("\${py.uri}")
        private lateinit var pyUri: String
        private val client: WebClient = WebClient.builder().baseUrl(pyUri).build()
    }

    fun getNearbyTensor(int: NDArray): NDArray? {
        return client.post().uri(RemoteConstV.Reduction.nearby).bodyValue(int).retrieve()
            .bodyToMono(object : ParameterizedTypeReference<NDArray>() {}).block()
    }

    fun getNearbyFromTo(from: Int, to: Int): List<NDArray>? {
        return client.post().uri(RemoteConstV.Reduction.fromTo).retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<NDArray>>() {}).block()
    }

    fun saveTensor(tensor: NDArray) {
        // in real don't know whether it has do successfully
        client.post().uri(RemoteConstV.Reduction.save).bodyValue(tensor).retrieve()
    }
}
