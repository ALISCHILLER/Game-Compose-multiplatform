package com.msa.compose_kmm.domain

import java.io.FileNotFoundException
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread

/**
 * پیاده‌سازی Desktop/JVM برای صدای بازی.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AudioPlayer {

    private val audioCache = mutableMapOf<String, ByteArray>()
    private val playingLines = mutableMapOf<String, SourceDataLine>()
    private val loopingSounds = mutableSetOf<String>()

    actual fun playGameOverSound() {
        stopFallingSound()
        playSound(fileName = "game_over.wav")
    }

    actual fun playJumpSound() {
        stopFallingSound()
        playSound(fileName = "jump.wav")
    }

    actual fun playFallingSound() {
        playSound(fileName = "falling.wav")
    }

    actual fun stopFallingSound() {
        stopSound(fileName = "falling.wav")
    }

    actual fun playGameSoundInLoop() {
        playSound(fileName = "game_sound.wav", loop = true)
    }

    actual fun stopGameSound() {
        stopSound(fileName = "game_sound.wav")
    }

    actual fun release() {
        stopAllSounds()
        audioCache.clear()
    }

    private fun playSound(
        fileName: String,
        loop: Boolean = false
    ) {
        thread(name = "audio-$fileName", isDaemon = true) {
            try {
                val audioData = audioCache[fileName] ?: loadAudioFile(fileName).also {
                    audioCache[fileName] = it
                }

                if (loop) {
                    synchronized(loopingSounds) {
                        loopingSounds.add(fileName)
                    }
                }

                do {
                    playAudioBytes(
                        fileName = fileName,
                        audioData = audioData
                    )
                } while (loop && isLoopRequested(fileName))
            } catch (error: Exception) {
                println("Error playing audio '$fileName': $error")
            }
        }
    }

    private fun playAudioBytes(
        fileName: String,
        audioData: ByteArray
    ) {
        AudioSystem.getAudioInputStream(audioData.inputStream()).use { inputStream ->
            val format = inputStream.format
            val info = DataLine.Info(SourceDataLine::class.java, format)
            val line = AudioSystem.getLine(info) as SourceDataLine

            line.open(format)
            line.start()

            synchronized(playingLines) {
                playingLines[fileName] = line
            }

            val buffer = ByteArray(BUFFER_SIZE)
            var shouldContinue = true

            while (shouldContinue) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == END_OF_STREAM) break

                synchronized(playingLines) {
                    shouldContinue = playingLines[fileName] === line
                }

                if (shouldContinue) {
                    line.write(buffer, 0, bytesRead)
                }
            }

            line.drain()
            line.close()

            synchronized(playingLines) {
                if (playingLines[fileName] === line) {
                    playingLines.remove(fileName)
                }
            }
        }
    }

    private fun stopSound(fileName: String) {
        synchronized(playingLines) {
            synchronized(loopingSounds) {
                loopingSounds.remove(fileName)
            }

            playingLines.remove(fileName)?.let { line ->
                line.stop()
                line.close()
            }
        }
    }

    private fun stopAllSounds() {
        synchronized(playingLines) {
            synchronized(loopingSounds) {
                loopingSounds.clear()
            }

            playingLines.values.forEach { line ->
                line.stop()
                line.close()
            }

            playingLines.clear()
        }
    }

    private fun isLoopRequested(fileName: String): Boolean {
        return synchronized(loopingSounds) {
            loopingSounds.contains(fileName)
        }
    }

    private fun loadAudioFile(fileName: String): ByteArray {
        val resourcePath = "composeResources/compose_kmm.composeapp.generated.resources/files/$fileName"

        val resourceStream = this::class.java.classLoader?.getResourceAsStream(resourcePath)
            ?: throw FileNotFoundException("Resource not found: $resourcePath")

        return resourceStream.use { it.readBytes() }
    }

    private companion object {
        const val BUFFER_SIZE = 4096
        const val END_OF_STREAM = -1
    }
}