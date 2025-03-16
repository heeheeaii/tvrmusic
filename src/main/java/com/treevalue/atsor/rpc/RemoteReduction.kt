package com.treevalue.atsor.rpc

import ai.djl.ndarray.NDArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class RemoteReduction {
    companion object {
        @Value("\${py.uri}")
        private lateinit var pyUri: String // todo
        private val client: WebClient = WebClient.builder().baseUrl("http://127.0.0.1:12000").build()
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
