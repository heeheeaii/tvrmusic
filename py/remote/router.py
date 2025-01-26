from fastapi import APIRouter
from remote.netTensor import NetTensor
from neuronalMemory.neuronalMemory import NeuralStorage
from neuronalMemory.task import TaskQueue

rRouter = APIRouter()


@rRouter.get("/save")
def save_tensor(key: int, tensor: NetTensor):
    TaskQueue().add(NeuralStorage().store, key, tensor)
