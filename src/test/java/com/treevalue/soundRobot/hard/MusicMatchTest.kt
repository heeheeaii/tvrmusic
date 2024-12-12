import com.treevalue.soundRobot.hard.MusicMatch
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import java.io.IOException

@ExtendWith(MockitoExtension::class)
class MusicMatchTest {
    private val resourceLoader: ResourceLoader = DefaultResourceLoader()

    @Test
    @Throws(IOException::class)
    fun testLoadMusicUrl_Success() {
        val musicPath = "static/music/"
        var musicMatch = MusicMatch(resourceLoader!!, musicPath)
        val url = musicMatch.RandomPath()
        println(url)
    }
}
