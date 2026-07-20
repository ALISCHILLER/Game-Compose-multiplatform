package com.msa.compose_kmm.domain

/** قرارداد مشترک پخش صدا؛ هر پیاده‌سازی مالک منابع پلتفرم خود است. */
interface AudioPlayer {
    fun setMusicVolume(volume: Float)
    fun setEffectsVolume(volume: Float)
    fun playGameOverSound()
    fun stopGameOverSound()
    fun playJumpSound()
    fun playScoreSound()
    fun playGameSoundInLoop()
    fun stopGameSound()
    fun release()
}
