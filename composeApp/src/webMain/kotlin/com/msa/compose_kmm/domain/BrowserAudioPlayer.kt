package com.msa.compose_kmm.domain

import compose_kmm.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.w3c.dom.Audio

/** HTMLAudioElement implementation shared in behavior by JS/Wasm targets. */
@OptIn(ExperimentalResourceApi::class)
class BrowserAudioPlayer : AudioPlayer {
    private val audioElements = mutableMapOf<String, Audio>()
    private var released = false
    private var musicVolume = DEFAULT_MUSIC_VOLUME
    private var effectsVolume = DEFAULT_EFFECTS_VOLUME


    override fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        audioElements[GAME_SOUND_FILE]?.volume = musicVolume.toDouble()
    }

    override fun setEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        audioElements.filterKeys { it != GAME_SOUND_FILE }.values.forEach { audio ->
            audio.volume = effectsVolume.toDouble()
        }
    }

    override fun playGameOverSound() = playSound("game_over.wav", restart = true)
    override fun stopGameOverSound() = stopSound("game_over.wav")
    override fun playJumpSound() = playSound("jump.wav", restart = true)
    override fun playScoreSound() = playSound("score.wav", restart = true)
    override fun playGameSoundInLoop() = playSound("game_sound.wav", loop = true)
    override fun stopGameSound() = stopSound("game_sound.wav")

    override fun release() {
        if (released) return
        released = true
        audioElements.values.forEach { audio ->
            runCatching {
                audio.pause()
                audio.currentTime = 0.0
                audio.src = ""
            }
        }
        audioElements.clear()
    }

    private fun playSound(fileName: String, loop: Boolean = false, restart: Boolean = false) {
        if (released) return
        runCatching {
            val audio = audioElements[fileName] ?: createAudioElement(fileName).also {
                audioElements[fileName] = it
            }
            audio.loop = loop
            audio.volume = (if (fileName == GAME_SOUND_FILE) musicVolume else effectsVolume).toDouble()
            if (restart) audio.currentTime = 0.0
            audio.play().catch { error ->
                println("Unable to play '$fileName': $error")
                error
            }
        }.onFailure { error ->
            println("Unable to prepare '$fileName': ${error.message}")
        }
    }

    private fun stopSound(fileName: String) {
        audioElements[fileName]?.let { audio ->
            runCatching {
                audio.pause()
                audio.currentTime = 0.0
            }
        }
    }

    private fun createAudioElement(fileName: String): Audio {
        val path = Res.getUri("files/$fileName")
        return Audio(path).apply {
            preload = "auto"
            onerror = {
                println("Unable to load audio resource: $path")
                null
            }
        }
    }

    private companion object {
        const val GAME_SOUND_FILE = "game_sound.wav"
        const val DEFAULT_MUSIC_VOLUME = 0.65f
        const val DEFAULT_EFFECTS_VOLUME = 0.85f
    }
}

