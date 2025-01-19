import tensorflow as tf
import keras
import numpy as np
import uuid
from collections import OrderedDict

class NeuralStorage(keras.Model):
    def __init__(self, input_size, encoding_size, memory_capacity=10):
        super(NeuralStorage, self).__init__()
        self.input_size = input_size
        self.encoding_size = encoding_size
        self.memory_capacity = memory_capacity
        self.memory = OrderedDict()

        self.encoder = keras.Sequential([
            keras.layers.Dense(256, activation='relu'),
            keras.layers.Dropout(0.2),
            keras.layers.Dense(encoding_size, activation='tanh'),  # add
            keras.layers.Dropout(0.2),  # add
            keras.layers.Dense(encoding_size, activation='tanh')
        ])

        # 解码器
        self.decoder = keras.Sequential([
            keras.layers.Dense(256, activation='relu'),
            keras.layers.Dropout(0.2),
            keras.layers.Dense(encoding_size, activation='tanh'),  # add
            keras.layers.Dropout(0.2),  # add
            keras.layers.Dense(input_size, activation='sigmoid')
        ])

    def call(self, x):
        encoded = self.encoder(x)
        decoded = self.decoder(encoded)
        return decoded

    def train_step(self, data):
        with tf.GradientTape() as tape:
            decoded = self(data)
            loss = keras.losses.MeanSquaredError()(data, decoded)
        gradients = tape.gradient(loss, self.trainable_variables)
        self.optimizer.apply_gradients(zip(gradients, self.trainable_variables))
        return {"loss": loss}

    def store(self, key, data):
        input_data = self.preprocess_data(data)
        self.train(input_data)
        self.memory[key] = input_data
        self._enforce_memory_capacity()
        return input_data

    def retrieve(self, key):
        if key in self.memory:
            output = self(self.memory[key])
            return self.postprocess_data(output)
        else:
            return None

    def store_and_retrieve(self, key, data):
        encoded_data = self.store(key, data)
        return self.retrieve(key)

    def preprocess_data(self, data):
        if isinstance(data, str):
            data = np.frombuffer(data.encode('utf-8'), dtype=np.uint8)
        elif isinstance(data, bytes):
            data = np.frombuffer(data, dtype=np.uint8)
        elif isinstance(data, np.ndarray):
            pass  # 如果已经是 NumPy 数组，则不进行处理
        else:
            raise ValueError("Unsupported data type")

        # 将数据缩放到 [0, 1] 范围
        data = data.astype(np.float32) / 255.0

        # 填充或截断数据以匹配输入大小
        if data.size < self.input_size:
            padded_data = np.pad(data, (0, self.input_size - data.size), 'constant')
        elif data.size > self.input_size:
            padded_data = data[:self.input_size]
        else:
            padded_data = data

        return tf.convert_to_tensor(padded_data.reshape(1, -1), dtype=tf.float32)

    def postprocess_data(self, output):
        output_data = output.numpy().flatten()
        output_data = (output_data * 255).astype(np.uint8)
        return output_data.tobytes()

    def train(self, data, epochs=200):
        self.optimizer = keras.optimizers.AdamW(learning_rate=0.0005)
        for epoch in range(epochs):
            metrics = self.train_step(data)
            if epoch % 20 == 0:
                print(f"Epoch: {epoch}, Loss: {metrics['loss']}")

    def _enforce_memory_capacity(self):
        while len(self.memory) > self.memory_capacity:
            oldest_key = next(iter(self.memory))
            del self.memory[oldest_key]


if __name__ == "__main__":
    input_size = 1024  # 输入数据大小（假设最大1024字节）
    encoding_size = 512  # 压缩后的编码大小
    memory_capacity = 5  # 存储容量
    storage = NeuralStorage(input_size, encoding_size, memory_capacity)

    # 存储和恢复图像数据
    image_data1 = b'\x00\x01\x02\x03\x04\x05\x06\x07\x08\t'
    key1 = uuid.uuid4()
    output_image1 = storage.store_and_retrieve(key1, image_data1)

    image_data2 = b'\x10\x11\x12\x13\x14\x15\x16\x17\x18\x19'
    key2 = uuid.uuid4()
    output_image2 = storage.store_and_retrieve(key2, image_data2)

    image_data3 = b'\x20\x21\x22\x23\x24\x25\x26\x27\x28\x29'
    key3 = uuid.uuid4()
    output_image3 = storage.store_and_retrieve(key3, image_data3)

    image_data4 = b'\x30\x31\x32\x33\x34\x35\x36\x37\x38\x39'
    key4 = uuid.uuid4()
    output_image4 = storage.store_and_retrieve(key4, image_data4)

    image_data5 = b'\x40\x41\x42\x43\x44\x45\x46\x47\x48\x49'
    key5 = uuid.uuid4()
    output_image5 = storage.store_and_retrieve(key5, image_data5)

    image_data6 = b'\x50\x51\x52\x53\x54\x55\x56\x57\x58\x59' * 100
    key6 = uuid.uuid4()
    output_image6 = storage.store_and_retrieve(key6, image_data6)

    # 尝试检索旧的图像
    retrieved_image1 = storage.retrieve(key1)
    if retrieved_image1:
        print(f"img1 origin:{image_data1[:10]}...")
        print(f"Retrieved Image 1: {retrieved_image1[:10]}...")
    else:
        print("Image 1 was forgotten.")

    retrieved_image2 = storage.retrieve(key2)
    if retrieved_image2:
        print(f"img2 origin:{image_data2[:10]}...")
        print(f"Retrieved Image 2: {retrieved_image2[:10]}...")
    else:
        print("Image 2 was forgotten.")

    retrieved_image6 = storage.retrieve(key6)
    count = 0
    idx = 0
    if retrieved_image6:
        while idx < len(retrieved_image6) and idx < len(image_data6):
            if retrieved_image6[idx] == image_data6[idx]:
                count += 1
            idx += 1
        print(f"all len {len(image_data6)}, now {count})")
    else:
        print("Image 6 was forgotten.")
