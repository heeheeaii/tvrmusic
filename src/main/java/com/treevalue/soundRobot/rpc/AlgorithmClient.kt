package com.treevalue.soundRobot.rpc

import io.grpc.ManagedChannelBuilder;

class AlgorithmClient {
    val stub: AlgorithmGrpc.AlgorithmFutureStub

    constructor() {
        val channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .build()
        stub = AlgorithmGrpc.newFutureStub(channel)
    }
}
