from fastapi import FastAPI
import uvicorn
from remote.router import rRouter

app = FastAPI()

app.include_router(rRouter, tags=["tensor"])


class AppRunner:
    def __init__(self, host='0.0.0.0', port=12000):
        self.host = host
        self.port = port

    def run(self):
        uvicorn.run(app, host=self.host, port=self.port)


if __name__ == '__main__':
    app_runner = AppRunner()
    app_runner.run()
