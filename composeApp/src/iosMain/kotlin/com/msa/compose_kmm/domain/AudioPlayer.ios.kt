package com.msa.compose_kmm.domain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.Foundation.NSURL.Companion.fileURLWithPath

/**
 * پیاده‌سازی iOS برای صدای بازی با AVAudioPlayer.
 *
 * نکته:
 * در Kotlin/Native بعضی propertyهای iOS مثل numberOfLoops با نوع Long map می‌شوند.
 * بنابراین مقدار loop باید Long باشد، نه Int.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer {

    private val audioPlayers = mutableMapOf<String, AVAudioPlayer?>()
    private var fallingSoundPlayer: AVAudioPlayer? = null

    init {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryAmbient, error = null)
        session.setActive(true, error = null)
    }

    actual fun playGameOverSound() {
        stopFallingSound()
        playSound(soundName = "game_over")
    }

    actual fun playJumpSound() {
        stopFallingSound()
        playSound(soundName = "jump")
    }

    actual fun playFallingSound() {
        if (fallingSoundPlayer != null) return
        fallingSoundPlayer = playSound(soundName = "falling")
    }

    actual fun stopFallingSound() {
        fallingSoundPlayer?.stop()
        fallingSoundPlayer = null
    }

    actual fun playGameSoundInLoop() {
        audioPlayers[GAME_SOUND_KEY]?.let { existingPlayer ->
            existingPlayer.play()
            return
        }

        val player = createPlayer(resourceName = GAME_SOUND_KEY)?.apply {
            numberOfLoops = LOOP_FOREVER
            prepareToPlay()
            play()
        }

        audioPlayers[GAME_SOUND_KEY] = player
    }

    actual fun stopGameSound() {
        audioPlayers[GAME_SOUND_KEY]?.stop()
        audioPlayers[GAME_SOUND_KEY] = null
    }

    actual fun release() {
        audioPlayers.values.forEach { it?.stop() }
        audioPlayers.clear()
        stopFallingSound()
    }

    private fun playSound(soundName: String): AVAudioPlayer? {
        val player = createPlayer(resourceName = soundName)?.apply {
            prepareToPlay()
            play()
        }

        audioPlayers[soundName] = player
        return player
    }

    private fun createPlayer(resourceName: String): AVAudioPlayer? {
        val url = getSoundURL(resourceName) ?: return null
        return AVAudioPlayer(url, error = null)
    }

    private fun getSoundURL(resourceName: String): NSURL? {
        val path = NSBundle.mainBundle().pathForResource(resourceName, "wav")
        return path?.let { fileURLWithPath(it) }
    }

    private companion object {
        const val GAME_SOUND_KEY = "game_sound"

        /**
         * مقدار -1 یعنی loop بی‌نهایت در AVAudioPlayer.
         *
         * نوع باید Long باشد، چون numberOfLoops در Kotlin/Native Long است.
         */
        const val LOOP_FOREVER: Long = -1L
    }
}