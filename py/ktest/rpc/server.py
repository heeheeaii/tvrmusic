import grpc
from concurrent import futures
import ktest.rpc.proto.Algorithm_pb2 as Algorithm_pb2
import ktest.rpc.proto.Algorithm_pb2_grpc as Algorithm_pb2_grpc


class AlgorithmImpl(Algorithm_pb2_grpc.Algorithm):
    def Echo(self, request, context):
        return Algorithm_pb2.EchoMsg(output='ecoh: ' + request.input)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    Algorithm_pb2_grpc.add_AlgorithmServicer_to_server(AlgorithmImpl(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started, listening on port 50051.")
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
