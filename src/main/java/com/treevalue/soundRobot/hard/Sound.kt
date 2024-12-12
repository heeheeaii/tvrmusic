import com.mpatric.mp3agic.InvalidDataException
import com.mpatric.mp3agic.Mp3File
import com.mpatric.mp3agic.UnsupportedTagException
import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

object Sound {
    fun getMP3DurationInMillis(mp3FilePath: String): Long {
        return try {
            val mp3File = Mp3File(mp3FilePath)
            mp3File.lengthInSeconds * 1000
        } catch (e: UnsupportedTagException) {
            -1
        } catch (e: InvalidDataException) {
            -1
        } catch (e: IOException) {
            -1
        }
    }

    fun convertMp3ToWav(mp3FilePath: String, wavFilePath: String) {
        try {
            val mp3File = File(mp3FilePath)
            if (!mp3File.exists()) {
                throw IllegalArgumentException("MP3 not exist: $mp3FilePath")
            }

            val audioInputStream = AudioSystem.getAudioInputStream(mp3File)

            val sourceFormat = audioInputStream.format

            val targetFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.sampleRate,
                16,
                sourceFormat.channels,
                sourceFormat.channels * 2,
                sourceFormat.sampleRate,
                false
            )

            val conversionStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream)

            val wavFile = File(wavFilePath)
            AudioSystem.write(conversionStream, AudioFileFormat.Type.WAVE, wavFile)

            audioInputStream.close()
            conversionStream.close()
        } catch (e: Exception) {
            println("error appear: ${e.message}")
        }
    }
}
