import com.treevalue.atsor.hard.io.CmdUtil
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class CmdTest {
    @Test
    fun progressBarTest() {
        val totalSteps = 100
        var currentStep = 0
        thread {
            while (currentStep <= totalSteps) {
                CmdUtil.printProgress(currentStep, totalSteps)
                currentStep++
            }
        }.join()
    }
}
