package com.treevalue.soundRobot.rpc//package com.treevalue.soundRobot.rpc

import io.grpc.stub.StreamObserver

class AlgorithmServer : AlgorithmGrpc.AlgorithmImplBase() {
    override fun echo(request: InputMsg, responseObserver: StreamObserver<OutputMsg>) {
        val reply = OutputMsg.newBuilder().setMessage("Echo: ${request.message}").build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }
}
