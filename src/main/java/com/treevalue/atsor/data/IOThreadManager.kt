import com.treevalue.atsor.data.ThreadManager
import kotlinx.coroutines.*

object IOThreadManager : ThreadManager(Dispatchers.IO + SupervisorJob())
