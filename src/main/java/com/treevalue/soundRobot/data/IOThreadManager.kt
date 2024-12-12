import com.treevalue.soundRobot.data.ThreadManager
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object IOThreadManager : ThreadManager(Dispatchers.IO + SupervisorJob())
