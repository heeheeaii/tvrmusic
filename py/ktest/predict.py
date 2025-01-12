import tensorflow as tf
import keras
from keras import layers, models  # 导入 models
import numpy as np

class MyCustomLayer(layers.Layer):
    def __init__(self, units=32, activation=None, **kwargs):
        super(MyCustomLayer, self).__init__(**kwargs)
        self.units = units
        self.activation = keras.activations.get(activation)

    def build(self, input_shape):
        self.w = self.add_weight(shape=(input_shape[-1], self.units),
                                 initializer="random_normal",
                                 trainable=True,
                                 name="my_weights")
        self.b = self.add_weight(shape=(self.units,),
                                 initializer="zeros",
                                 trainable=True,
                                 name="my_bias")
        super().build(input_shape)

    def call(self, inputs):
        output = tf.matmul(inputs, self.w) + self.b
        if self.activation is not None:
            output = self.activation(output)
        return output

    def get_config(self):
        config = super().get_config()
        config.update({
            "units": self.units,
            "activation": keras.activations.serialize(self.activation)
        })
        return config

# Example usage:

# Create a model using the custom layer
model = models.Sequential([
    layers.Dense(64, activation='relu', input_shape=(10,)),
    MyCustomLayer(32, activation="relu"),
    layers.Dense(10, activation='softmax')
])

# Compile the model
model.compile(optimizer='adam',
              loss='categorical_crossentropy',
              metrics=['accuracy'])

# Example dummy data (replace with your actual data)
input_data = tf.random.normal((100, 10))
labels = tf.random.uniform((100,), maxval=10, dtype=tf.int32)
labels = tf.one_hot(labels, depth=10)  # One-hot encode the labels

# Train the model
model.fit(input_data, labels, epochs=10)

# Save the model
model.save(R"D:\agi\tvrmusicnew\py\ktest\m.keras")

# Load the model
loaded_model = models.load_model(R"D:\agi\tvrmusicnew\py\ktest\m.keras", custom_objects={'MyCustomLayer': MyCustomLayer})

# Print the model summary
print(loaded_model.summary())
