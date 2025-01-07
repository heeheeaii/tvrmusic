package com.treevalue.soundRobot

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


class ReactiveTest {
    @Test
    fun monoFluxTest() {
        val singleItem = Mono.just("Hello, Reactive World!")
        singleItem.subscribe { item: String -> println("Received: $item") }
        val multipleItems = Flux.just("Item1", "Item2", "Item3", "item4")
        multipleItems.subscribe { item: String -> println("Received: $item") }
    }
}
