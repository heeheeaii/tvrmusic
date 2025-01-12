import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GlobalExceptionHandler : Thread.UncaughtExceptionHandler {
    //use in: Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler())
    // then use in thread
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    override fun uncaughtException(t: Thread, e: Throwable) {
        logger.error("Uncaught exception in thread: {}, Exception: {}", t.name, e.message)
        (e as? ArithmeticException)?.let {
        } ?: (e as? NullPointerException)?.let {
        } ?: (e as? OutOfMemoryError)?.let {
        }
    }
}
