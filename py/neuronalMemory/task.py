import asyncio
import logging
from util.singleton import singleton


@singleton
class TaskQueue:
    def __init__(self, max_size=12000):
        self.queue = asyncio.Queue(maxsize=max_size)
        self.task_id_counter = 0

    async def add(self, task_func, *args, **kwargs):
        task_id = self.task_id_counter
        self.task_id_counter += 1
        if self.queue.full():
            old_task = await self.queue.get()
        task = {
            'id': task_id,
            'func': task_func,
            'args': args,
            'kwargs': kwargs
        }
        await self.queue.put(task)

    async def exec(self):
        while True:
            task = await self.queue.get()
            task_id = task['id']
            func = task['func']
            args = task['args']
            kwargs = task['kwargs']
            try:
                await func(*args, **kwargs)
            except Exception as e:
                print(e)
                pass
            finally:
                self.queue.task_done()

    async def run(self):
        await self.queue.join()


async def __test_task(task_id, duration):
    logging.info(f"Task {task_id}: Starting, will sleep for {duration} seconds.")
    await asyncio.sleep(0.1)


async def __test_f():
    task_queue = TaskQueue(max_size=10)
    for i in range(20):
        await task_queue.add(__test_task, i, i % 5 + 1)
    asyncio.create_task(task_queue.exec())
    await task_queue.run()


if __name__ == "__main__":
    asyncio.run(__test_f())
