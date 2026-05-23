package com.msa.compose_kmm.domain


/**
 * قرارداد مشترک پخش صدا.
 *
 * پیاده‌سازی واقعی برای هر پلتفرم جداست:
 * - Android: SoundPool + ExoPlayer
 * - Desktop: javax.sound.sampled
 * - iOS: AVAudioPlayer
 * - Web/Wasm: HTMLAudioElement
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AudioPlayer {

    /** پخش صدای پایان بازی. */
    fun playGameOverSound()

    /** پخش صدای پرش. */
    fun playJumpSound()

    /** پخش صدای سقوط. */
    fun playFallingSound()

    /** توقف صدای سقوط. */
    fun stopFallingSound()

    /** پخش صدای پس‌زمینه به صورت loop. */
    fun playGameSoundInLoop()

    /** توقف صدای پس‌زمینه. */
    fun stopGameSound()

    /** آزادسازی منابع صوتی. */
    fun release()
}

/**
 * مسیر فایل‌های صوتی داخل Compose Resources.
 */
val soundResList = listOf(
    "files/falling.wav",
    "files/game_over.wav",
    "files/game_sound.wav",
    "files/jump.wav"
)