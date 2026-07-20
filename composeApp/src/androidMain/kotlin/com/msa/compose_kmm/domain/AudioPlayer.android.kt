package com.msa.compose_kmm.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.msa.compose_kmm.R
import java.util.Collections

/** Android audio: SoundPool برای افکت‌ها و ExoPlayer برای موسیقی پس‌زمینه. */
class AndroidAudioPlayer(context: Context) : AudioPlayer {
    private val appContext = context.applicationContext
    private var released = false
    private var musicVolume = DEFAULT_MUSIC_VOLUME
    private var effectsVolume = DEFAULT_EFFECTS_VOLUME
    private val loadedSoundIds = Collections.synchronizedSet(mutableSetOf<Int>())

    private val loopingPlayer = ExoPlayer.Builder(appContext).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        setAudioAttributes(
            androidx.media3.common.AudioAttributes.Builder()
                .setUsage(C.USAGE_GAME)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        val uri = Uri.parse("android.resource://${appContext.packageName}/${R.raw.game_sound}")
        setMediaItem(MediaItem.fromUri(uri))
        volume = musicVolume
        prepare()
    }

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_PARALLEL_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
        .also { pool ->
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (!released && status == LOAD_SUCCESS) loadedSoundIds += sampleId
            }
        }

    private val jumpSound = soundPool.load(appContext, R.raw.jump, PRIORITY_HIGH)
    private val gameOverSound = soundPool.load(appContext, R.raw.game_over, PRIORITY_NORMAL)
    private val scoreSound = soundPool.load(appContext, R.raw.score, PRIORITY_HIGH)
    private var gameOverStreamId = NO_STREAM_ID


    override fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        if (!released) loopingPlayer.volume = musicVolume
    }

    override fun setEffectsVolume(volume: Float) {
        effectsVolume = volume.coerceIn(0f, 1f)
        if (!released && gameOverStreamId != NO_STREAM_ID) {
            soundPool.setVolume(gameOverStreamId, effectsVolume, effectsVolume)
        }
    }

    override fun playGameOverSound() {
        if (released) return
        stopGameOverSound()
        gameOverStreamId = playEffect(gameOverSound, PRIORITY_NORMAL)
    }

    override fun stopGameOverSound() {
        if (released || gameOverStreamId == NO_STREAM_ID) return
        soundPool.stop(gameOverStreamId)
        gameOverStreamId = NO_STREAM_ID
    }

    override fun playScoreSound() {
        if (released) return
        playEffect(scoreSound, PRIORITY_HIGH)
    }

    override fun playJumpSound() {
        if (released) return
        playEffect(jumpSound, PRIORITY_HIGH)
    }

    override fun playGameSoundInLoop() {
        if (!released && !loopingPlayer.isPlaying) loopingPlayer.play()
    }

    override fun stopGameSound() {
        if (!released) loopingPlayer.pause()
    }

    override fun release() {
        if (released) return
        stopGameOverSound()
        released = true
        loopingPlayer.release()
        soundPool.release()
        loadedSoundIds.clear()
    }

    private fun playEffect(soundId: Int, priority: Int): Int {
        if (soundId !in loadedSoundIds) return NO_STREAM_ID
        return soundPool.play(soundId, effectsVolume, effectsVolume, priority, NO_LOOP, NORMAL_RATE)
    }

    private companion object {
        const val MAX_PARALLEL_STREAMS = 4
        const val DEFAULT_MUSIC_VOLUME = 0.65f
        const val DEFAULT_EFFECTS_VOLUME = 0.85f
        const val NORMAL_RATE = 1f
        const val PRIORITY_HIGH = 2
        const val PRIORITY_NORMAL = 1
        const val NO_LOOP = 0
        const val NO_STREAM_ID = 0
        const val LOAD_SUCCESS = 0
    }
}
