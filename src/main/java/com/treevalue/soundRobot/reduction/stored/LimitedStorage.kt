import kotlin.math.pow
import kotlin.random.Random

data class TensorData(
    val tensor: Array<FloatArray>,
    var attentionScore: Float = 0f,
    var redundancyScore: Float = 0f,
    val sensorType: SensorType
)

enum class SensorType {
    VISUAL, AUDITORY
}

class LimitedStorage(private val capacity: Int) {
    private val storage = LinkedHashMap<String, TensorData>(capacity, 0.75f, true)

    fun calculateAttentionScore(tensor: Array<FloatArray>, sensorType: SensorType): Float {
        val flatTensor = tensor.flatMap { it.toList() }
        val average = flatTensor.average().toFloat()
        val variance =
            flatTensor.fold(0.0) { acc, x -> acc + (x - average).toDouble().pow(2.0) }.toFloat() / flatTensor.size
        val sensorWeight = if (sensorType == SensorType.VISUAL) 1.0f else 0.8f
        return (average + variance) * sensorWeight
    }

    fun calculateRedundancyScore(tensor: Array<FloatArray>, existingTensors: List<Array<FloatArray>>): Float {
        if (existingTensors.isEmpty()) return 0f
        var maxSimilarity = 0f
        for (existingTensor in existingTensors) {
            val similarity = calculateTensorSimilarity(tensor, existingTensor)
            maxSimilarity = maxOf(maxSimilarity, similarity)
        }
        return maxSimilarity
    }

    fun calculateTensorSimilarity(tensor1: Array<FloatArray>, tensor2: Array<FloatArray>): Float {
        val flatTensor1 = tensor1.flatMap { it.toList() }.toFloatArray()
        val flatTensor2 = tensor2.flatMap { it.toList() }.toFloatArray()
        if (flatTensor1.size != flatTensor2.size) return 0f
        val dotProduct = flatTensor1.zip(flatTensor2).sumOf { (a, b) -> (a * b).toDouble() }.toFloat()
        val magnitude1 = flatTensor1.sumOf { it.toDouble().pow(2.0) }.toFloat().pow(0.5f)
        val magnitude2 = flatTensor2.sumOf { it.toDouble().pow(2.0) }.toFloat().pow(0.5f)
        if (magnitude1 == 0f || magnitude2 == 0f) return 0f
        return dotProduct / (magnitude1 * magnitude2)
    }

    private fun evictLeastImportant(): String? {
        if (storage.isEmpty()) return null
        var minScore = Float.MAX_VALUE
        var minKey: String? = null
        for ((key, data) in storage) {
            val evictionScore = (1 - data.attentionScore) * 0.7f + data.redundancyScore * 0.3f
            if (evictionScore < minScore) {
                minScore = evictionScore
                minKey = key
            }
        }
        minKey?.let { storage.remove(it) }
        return minKey
    }

    fun store(key: String, tensor: Array<FloatArray>, sensorType: SensorType) {
        val attentionScore = calculateAttentionScore(tensor, sensorType)
        val redundancyScore = calculateRedundancyScore(tensor, storage.values.map { it.tensor })
        val data = TensorData(tensor, attentionScore, redundancyScore, sensorType)
        if (storage.size >= capacity) {
            evictLeastImportant()
        }
        storage[key] = data
    }

    fun get(key: String): TensorData? {
        return storage[key]
    }

    fun printStorage() {
        println("Current Storage:")
        storage.forEach { (key, data) ->
            println("Key: $key, Attention: ${data.attentionScore}, Redundancy: ${data.redundancyScore}, Sensor: ${data.sensorType}")
        }
    }
}

fun generateRandomTensor(rows: Int, cols: Int): Array<FloatArray> {
    return Array(rows) { FloatArray(cols) { Random.nextFloat() } }
}

fun main() {
    val storage = LimitedStorage(5)

    // 模拟存储数据
    for (i in 1..10) {
        val sensorType = if (i % 2 == 0) SensorType.VISUAL else SensorType.AUDITORY
        val tensor = generateRandomTensor(3, 3)
        storage.store("tensor$i", tensor, sensorType)
        println("Stored tensor$i")
        storage.printStorage()
        println("-------------------")
    }

    // 获取存储的数据
    val data = storage.get("tensor3")
    println("Retrieved tensor3: $data")
}
