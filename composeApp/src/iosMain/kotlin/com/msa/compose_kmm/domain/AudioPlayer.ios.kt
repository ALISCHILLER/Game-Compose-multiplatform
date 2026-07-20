package com.msa.compose_kmm.domain

import compose_kmm.composeapp.generated.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSURL
import platform.Foundation.NSURL.Companion.URLWithString

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
class IosAudioPlayer : AudioPlayer {
    private val players = mutableMapOf<String, AVAudioPlayer>()
    private var released = false
    private var musicVolume = DEFAULT_MUSIC_VOLUME
    private var effectsVolume = DEFAULT_EFFECTS_VOLUME

    init {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryAmbient, error = null)
        session.setActive(true, error = null)
    }


    override fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        players[GAME_SOUND_FILE]?.volume = musicVolume
    }

    override fun setEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        players.filterKeys { it != GAME_SOUND_FILE }.values.forEach { player ->
            player.volume = effectsVolume
        }
    }

    override fun playGameOverSound() = play("game_over.wav", restart = true)
    override fun stopGameOverSound() = stop("game_over.wav")
    override fun playJumpSound() = play("jump.wav", restart = true)
    override fun playScoreSound() = play("score.wav", restart = true)
    override fun playGameSoundInLoop() = play(GAME_SOUND_FILE, loop = true)
    override fun stopGameSound() = stop(GAME_SOUND_FILE)

    override fun release() {
        if (released) return
        released = true
        players.values.forEach { it.stop() }
        players.clear()
        AVAudioSession.sharedInstance().setActive(false, error = null)
    }

    private fun play(fileName: String, loop: Boolean = false, restart: Boolean = false) {
        if (released) return
        val player = players[fileName] ?: createPlayer(fileName)?.also {
            players[fileName] = it
        } ?: return

        player.volume = if (fileName == GAME_SOUND_FILE) musicVolume else effectsVolume
        if (restart) player.currentTime = 0.0
        player.numberOfLoops = if (loop) LOOP_FOREVER else 0L
        player.prepareToPlay()
        player.play()
    }

    private fun stop(fileName: String) {
        players[fileName]?.let { player ->
            player.stop()
            player.currentTime = 0.0
        }
    }

    private fun createPlayer(fileName: String): AVAudioPlayer? {
        val resourceUri = Res.getUri("files/$fileName")
        val url: NSURL = NSURL.URLWithString(resourceUri) ?: return null
        return AVAudioPlayer(url, error = null)
    }

    private companion object {
        const val GAME_SOUND_FILE = "game_sound.wav"
        const val LOOP_FOREVER: Long = -1L
        const val DEFAULT_MUSIC_VOLUME: Float = 0.65f
        const val DEFAULT_EFFECTS_VOLUME: Float = 0.85f
    }
}
