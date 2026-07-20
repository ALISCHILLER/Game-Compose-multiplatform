package com.msa.compose_kmm.domain

import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import kotlin.math.log10

/** پیاده‌سازی Desktop با Clipهای قابل استفاده مجدد و بدون ساخت Thread برای هر پرش. */
class DesktopAudioPlayer : AudioPlayer {
    private val lock = Any()
    private val audioBytes = mutableMapOf<String, ByteArray>()
    private val clips = mutableMapOf<String, Clip>()
    private var released = false
    private var musicVolume = DEFAULT_MUSIC_VOLUME
    private var effectsVolume = DEFAULT_EFFECTS_VOLUME


    override fun setMusicVolume(volume: Float) {
        synchronized(lock) {
            musicVolume = volume.coerceIn(0f, 1f)
            clips[GAME_SOUND_FILE]?.let { applyVolume(it, musicVolume) }
        }
    }

    override fun setEffectsVolume(volume: Float) {
        synchronized(lock) {
            effectsVolume = volume.coerceIn(0f, 1f)
            clips.filterKeys { it != GAME_SOUND_FILE }.values.forEach { clip ->
                applyVolume(clip, effectsVolume)
            }
        }
    }

    override fun playGameOverSound() = play("game_over.wav", restart = true)
    override fun stopGameOverSound() = stop("game_over.wav")
    override fun playJumpSound() = play("jump.wav", restart = true)
    override fun playScoreSound() = play("score.wav", restart = true)
    override fun playGameSoundInLoop() = play("game_sound.wav", loop = true)
    override fun stopGameSound() = stop("game_sound.wav")

    override fun release() {
        synchronized(lock) {
            if (released) return
            released = true
            clips.values.forEach { clip ->
                runCatching { clip.stop() }
                runCatching { clip.close() }
            }
            clips.clear()
            audioBytes.clear()
        }
    }

    private fun play(fileName: String, loop: Boolean = false, restart: Boolean = false) {
        runCatching {
            synchronized(lock) {
                if (released) return
                val clip = clips[fileName] ?: createClip(fileName).also { clips[fileName] = it }
                applyVolume(clip, if (fileName == GAME_SOUND_FILE) musicVolume else effectsVolume)
                if (restart) {
                    clip.stop()
                    clip.framePosition = 0
                } else if (!clip.isRunning) {
                    clip.framePosition = 0
                }
                if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY) else clip.start()
            }
        }.onFailure { error ->
            println("Unable to play '$fileName': ${error.message}")
        }
    }

    private fun stop(fileName: String) {
        synchronized(lock) {
            val clip = clips[fileName] ?: return
            runCatching { clip.stop() }
            runCatching { clip.framePosition = 0 }
        }
    }

    private fun createClip(fileName: String): Clip {
        val bytes = audioBytes[fileName] ?: loadAudioFile(fileName).also { audioBytes[fileName] = it }
        val clip = AudioSystem.getClip()
        AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes)).use { stream ->
            clip.open(stream)
        }
        return clip
    }


    private fun applyVolume(clip: Clip, volume: Float) {
        runCatching {
            val control = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            val gain = if (volume <= 0f) {
                control.minimum
            } else {
                (20f * log10(volume)).coerceIn(control.minimum, control.maximum)
            }
            control.value = gain
        }
    }

    private fun loadAudioFile(fileName: String): ByteArray {
        val candidates = listOf(
            "composeResources/compose_kmm.composeapp.generated.resources/files/$fileName",
            "files/$fileName",
            fileName
        )
        val classLoader = this::class.java.classLoader
        val stream = candidates.firstNotNullOfOrNull { path -> classLoader?.getResourceAsStream(path) }
            ?: throw FileNotFoundException("Audio resource not found: $fileName")
        return stream.use { it.readBytes() }
    }

    private companion object {
        const val GAME_SOUND_FILE = "game_sound.wav"
        const val DEFAULT_MUSIC_VOLUME = 0.65f
        const val DEFAULT_EFFECTS_VOLUME = 0.85f
    }
}

