package com.treevalue.atsor.rpc

import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

import io.grpc.stub.StreamObserver


class GrpcTest {

    class AlgorithmServer : AlgorithmGrpc.AlgorithmImplBase() {
        override fun echo(request: InputMsg, responseObserver: StreamObserver<OutputMsg>) {
            val reply = OutputMsg.newBuilder().setMessage("Echo: ${request.message}").build()
            responseObserver.onNext(reply)
            responseObserver.onCompleted()
        }
    }

    private lateinit var server: Server
    private lateinit var channel: io.grpc.ManagedChannel
    private lateinit var stub: AlgorithmGrpc.AlgorithmBlockingStub

    @BeforeEach
    fun setUp() {

        server = ServerBuilder.forPort(50051)
            .addService(AlgorithmServer())
            .build()
            .start()
        println("Server started, listening on 50051")
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .build()
        stub = AlgorithmGrpc.newBlockingStub(channel)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
        server.awaitTermination(1, TimeUnit.SECONDS)
        channel.shutdown()
        channel.awaitTermination(1, TimeUnit.SECONDS)
    }

    @Test
    fun testEcho() {
        val request = InputMsg.newBuilder().setMessage("test").build()
        val response = stub.echo(request)
        Assertions.assertEquals("Echo: test", response.message)
    }
}
