import tensorflow as tf
from keras import layers, models
import numpy as np

print("Num GPUs Available: ", len(tf.config.experimental.list_physical_devices('GPU')))
