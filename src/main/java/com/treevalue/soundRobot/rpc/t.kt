package com.treevalue.soundRobot.rpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

class GreeterService : GreeterGrpc.GreeterImplBase() {
    override fun sayHello(request: Helloword.HelloRequest, responseObserver: StreamObserver<Helloword.HelloReply>) {
        val reply = Helloword.HelloReply.newBuilder().setMessage("Hello, ${request.name}").build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }
}

fun main() {
    val server: Server = ServerBuilder.forPort(50051)
        .addService(GreeterService())
        .build()
        .start()

    println("Server started, listening on 50051")
    Runtime.getRuntime().addShutdownHook(Thread {
        println("*** shutting down gRPC server since JVM is shutting down")
        server.shutdown()
        println("*** server shut down")
    })

    server.awaitTermination()
}
