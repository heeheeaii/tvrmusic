import grpc
import ktest.rpc.helloword_pb2 as helloword_pb2
import ktest.rpc.helloword_pb2_grpc as helloword_pb2_grpc


def run():
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = helloword_pb2_grpc.GreeterStub(channel)
        response = stub.SayHello(helloword_pb2.HelloRequest(name='World'))
        print("Greeter client received: " + response.message)


if __name__ == '__main__':
    run()
