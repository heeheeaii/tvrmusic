from fastapi import FastAPI
import uvicorn
from rpc.netTensor import NetTensor

app = FastAPI()


@app.get("/search")
def get_search(tensor: NetTensor):
    pass


if __name__ == '__main__':
    tensor1 = NetTensor(shape=[2, 2], data=[1.0, 2.0, 3.0, 4.0])
    print(tensor1)
    print(tensor1.shape)
    print(tensor1.data)

    tensor2 = NetTensor(shape=[4], data=[1, 2, 3])
    print(tensor2)
    uvicorn.run(app, host='0.0.0.0', port=12000)
