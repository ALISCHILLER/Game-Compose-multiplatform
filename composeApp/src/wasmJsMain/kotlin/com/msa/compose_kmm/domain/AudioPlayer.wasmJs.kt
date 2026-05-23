package com.msa.compose_kmm.domain

import compose_kmm.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.w3c.dom.Audio

/**
 * پیاده‌سازی Web/Wasm برای صدا با HTMLAudioElement.
 */
@OptIn(ExperimentalResourceApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AudioPlayer {

    private val audioElements = mutableMapOf<String, Audio>()

    actual fun playGameOverSound() {
        stopFallingSound()
        playSound(fileName = "game_over.wav")
    }

    actual fun playJumpSound() {
        stopFallingSound()
        playSound(fileName = "jump.wav", restart = true)
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
        audioElements.clear()
    }

    private fun playSound(
        fileName: String,
        loop: Boolean = false,
        restart: Boolean = false
    ) {
        val audio = audioElements[fileName] ?: createAudioElement(fileName).also {
            audioElements[fileName] = it
        }

        audio.loop = loop

        if (restart) {
            audio.currentTime = 0.0
        }

        audio.play().catch { error ->
            println("Error playing sound: $fileName - $error")
            error
        }
    }

    private fun stopSound(fileName: String) {
        audioElements[fileName]?.let { audio ->
            audio.pause()
            audio.currentTime = 0.0
        }
    }

    private fun stopAllSounds() {
        audioElements.values.forEach { audio ->
            audio.pause()
            audio.currentTime = 0.0
        }
    }

    private fun createAudioElement(fileName: String): Audio {
        val path = Res.getUri("files/$fileName")

        return Audio(path).apply {
            onerror = { _, _, _, _, _ ->
                println("Error loading audio file: $path")
                null
            }
        }
    }
}