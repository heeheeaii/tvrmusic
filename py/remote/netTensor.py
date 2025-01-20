from typing import List
from pydantic import BaseModel, model_validator

class NetTensor(BaseModel):
    shape: List[int]
    data: List[float]

    @model_validator(mode='after')
    def validate_tensor(self) -> 'NetTensor':
        if not all(x > 0 for x in self.shape):
            self.shape = []
            self.data = []
            return self

        expected_size = 1
        for dim in self.shape:
            expected_size *= dim

        current_size = len(self.data)
        if current_size != expected_size:
            if current_size < expected_size:
                self.data.extend([0.0] * (expected_size - current_size))
            else:
                self.data = self.data[:expected_size]

        return self
