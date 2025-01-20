from fastapi import APIRouter
from remote.netTensor import NetTensor

rRouter = APIRouter()


@rRouter.get("/save")
def save_tensor(key: int, tensor: NetTensor):
    return {"test": 1}
