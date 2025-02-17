import grpc

import rpc.Algorithm_pb2 as Algorithm_pb2
import rpc.Algorithm_pb2_grpc as Algorithm_pb2_grc

def run():
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = Algorithm_pb2_grc.AlgorithmStub(channel)
        response = stub.Echo(Algorithm_pb2.InputMsg(input='World'))
        print("Greeter client received: " + response.output)


if __name__ == '__main__':
    run()
