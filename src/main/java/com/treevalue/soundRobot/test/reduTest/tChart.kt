import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

fun simulatedNetworkRequest(): String {
    var a = 0
    var s = ""
    for (idx in 0 until 10000) {
        a++
        s = "" + a
    }
    return "Data from server"
}


fun main() {
    runBlocking {
        val numRequests = 1000

        // Kotlin Coroutines
        val coroutineTime = measureTimeMillis {
            coroutineScope {
                val deferreds = (1..numRequests).map {
                    async { simulatedNetworkRequest() }
                }
                deferreds.awaitAll()
            }
        }

        // Java Virtual Threads
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val virtualThreadTime = measureTimeMillis {
            val tasks = (1..numRequests).map {
                Runnable { simulatedNetworkRequest() }
            }
            tasks.forEach { executor.submit(it) }
            executor.shutdown()
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS) // Wait for completion
        }

        println("Kotlin Coroutines: $coroutineTime ms")
        println("Java Virtual Threads: $virtualThreadTime ms")

        executor.shutdownNow() // Ensure executor is shut down

    }
}
