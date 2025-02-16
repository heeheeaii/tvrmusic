import numpy as np
import tensorflow as tf
from keras import Model, Input, utils
from keras.src.layers import Conv2D, ReLU, BatchNormalization, Conv2DTranspose, MaxPooling2D


def SegmentationNet(input_shape, num_classes):
    """
    构建语义分割网络

    Args:
        input_shape: 输入张量的形状 (height, width, channels)
        num_classes: 类别数量

    Returns:
        Keras 模型
    """

    inputs = Input(shape=input_shape)

    x = Conv2D(64, (3, 3), padding='same')(inputs)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(64, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = MaxPooling2D((2, 2), strides=(2, 2))(x)

    x = Conv2D(128, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(128, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = MaxPooling2D((2, 2), strides=(2, 2))(x)

    x = Conv2D(256, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(256, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = MaxPooling2D((2, 2), strides=(2, 2))(x)

    x = Conv2D(512, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(512, (3, 3), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)

    x = Conv2DTranspose(256, (2, 2), strides=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)

    x = Conv2DTranspose(128, (2, 2), strides=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)

    x = Conv2DTranspose(64, (2, 2), strides=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)

    outputs = Conv2D(num_classes, (1, 1), activation='softmax')(x)

    model = Model(inputs=inputs, outputs=outputs)
    return model


input_shape = (224, 224, 3)
num_classes = 10
model = SegmentationNet(input_shape, num_classes)

model.compile(optimizer='adam',
              loss='categorical_crossentropy',
              metrics=['accuracy'])
dummy_input = tf.random.normal((1,
                                *input_shape))  # extract packet , input_shape 是一个元组，例如 (224, 224, 3)，表示图像的高度、宽度和通道数。*input_shape 将这个元组解包成三个独立的参数，即 224, 224, 3。

num_samples = 100
dummy_inputs = np.random.rand(num_samples, *input_shape)
dummy_labels = np.random.randint(0, num_classes, size=(num_samples, input_shape[0], input_shape[1]))
dummy_labels_onehot = utils.to_categorical(dummy_labels, num_classes=num_classes)

# 训练模型
batch_size = 16
epochs = 10
history = model.fit(
    dummy_inputs,
    dummy_labels_onehot,
    batch_size=batch_size,
    epochs=epochs,
    validation_split=0.2
)

model.summary()

# 你可以在训练后评估模型（如果需要）
#  loss, accuracy = model.evaluate(test_inputs, test_labels_onehot)
# print(f"Test Loss: {loss:.4f}")
# print(f"Test Accuracy: {accuracy:.4f}")

# 你也可以用训练好的模型进行预测（如果需要）
# predictions = model.predict(new_inputs)
