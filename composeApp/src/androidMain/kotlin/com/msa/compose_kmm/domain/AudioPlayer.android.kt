package com.msa.compose_kmm.domain

import android.content.Context
import android.media.SoundPool
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.msa.compose_kmm.R
import compose_kmm.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * پیاده‌سازی Android برای صدای بازی.
 *
 * SoundPool برای افکت‌های کوتاه مناسب است.
 * ExoPlayer برای صدای loop پس‌زمینه استفاده شده است.
 */
@OptIn(ExperimentalResourceApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AudioPlayer(context: Context) {

    private val appContext = context.applicationContext

    private val loopingPlayer = ExoPlayer.Builder(appContext).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        setMediaItem(MediaItem.fromUri(Res.getUri("files/game_sound.wav")))
        prepare()
    }

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_PARALLEL_STREAMS)
        .build()

    private val jumpSound = soundPool.load(appContext, R.raw.jump, PRIORITY_HIGH)
    private val fallingSound = soundPool.load(appContext, R.raw.falling, PRIORITY_NORMAL)
    private val gameOverSound = soundPool.load(appContext, R.raw.game_over, PRIORITY_NORMAL)

    private var fallingSoundStreamId: Int = NO_STREAM_ID

    actual fun playGameOverSound() {
        stopFallingSound()
        soundPool.play(gameOverSound, FULL_VOLUME, FULL_VOLUME, PRIORITY_NORMAL, NO_LOOP, NORMAL_RATE)
    }

    actual fun playJumpSound() {
        stopFallingSound()
        soundPool.play(jumpSound, FULL_VOLUME, FULL_VOLUME, PRIORITY_HIGH, NO_LOOP, NORMAL_RATE)
    }

    actual fun playFallingSound() {
        if (fallingSoundStreamId != NO_STREAM_ID) return

        fallingSoundStreamId = soundPool.play(
            fallingSound,
            FULL_VOLUME,
            FULL_VOLUME,
            PRIORITY_NORMAL,
            NO_LOOP,
            NORMAL_RATE
        )
    }

    actual fun stopFallingSound() {
        if (fallingSoundStreamId == NO_STREAM_ID) return

        soundPool.stop(fallingSoundStreamId)
        fallingSoundStreamId = NO_STREAM_ID
    }

    actual fun playGameSoundInLoop() {
        if (!loopingPlayer.isPlaying) {
            loopingPlayer.play()
        }
    }

    actual fun stopGameSound() {
        loopingPlayer.pause()
    }

    actual fun release() {
        stopFallingSound()
        loopingPlayer.release()
        soundPool.release()
    }

    private companion object {
        const val MAX_PARALLEL_STREAMS = 4
        const val FULL_VOLUME = 1f
        const val NORMAL_RATE = 1f
        const val PRIORITY_HIGH = 2
        const val PRIORITY_NORMAL = 1
        const val NO_LOOP = 0
        const val NO_STREAM_ID = 0
    }
}