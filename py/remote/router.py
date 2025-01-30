from fastapi import APIRouter
from remote.netTensor import NetTensor
from neuronalMemory.neuronalMemory import NeuralStorage
from neuronalMemory.task import TaskQueue

rRouter = APIRouter()


@rRouter.post("/reduction/save")
def save_tensor(key: int, tensor: NetTensor):
    TaskQueue().add(NeuralStorage().store, key, tensor)


@rRouter.get("/reduction/get")
def get_tensor(key_from: int, key_to_exclude: int):
    tensors = []
    NeuralStorage.retrieve(key_from)

@rRouter.get("/reduction/nearby")
def nearbyData(center: int):
    tensors = []
    NeuralStorage.retrieve()
