package com.msa.compose_kmm.domain

/**
 * تنظیمات immutable و مستقل از پلتفرم بازی.
 *
 * حجم‌ها به‌صورت عدد صحیح 0..100 ذخیره می‌شوند تا Codec بین همه Targetها دقیق،
 * پایدار و بدون اختلاف Float باشد.
 */
data class GameSettings(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val effectsEnabled: Boolean = true,
    val musicVolume: Int = DEFAULT_MUSIC_VOLUME,
    val effectsVolume: Int = DEFAULT_EFFECTS_VOLUME,
    val reduceMotion: Boolean = false,
    val showGameplayHints: Boolean = true
) {
    val effectiveMusicVolume: Float
        get() = if (soundEnabled && musicEnabled) musicVolume / 100f else 0f

    val effectiveEffectsVolume: Float
        get() = if (soundEnabled && effectsEnabled) effectsVolume / 100f else 0f

    fun normalized(): GameSettings = copy(
        schemaVersion = CURRENT_SCHEMA_VERSION,
        musicVolume = musicVolume.coerceIn(MIN_VOLUME, MAX_VOLUME),
        effectsVolume = effectsVolume.coerceIn(MIN_VOLUME, MAX_VOLUME)
    )

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1
        const val MIN_VOLUME: Int = 0
        const val MAX_VOLUME: Int = 100
        const val DEFAULT_MUSIC_VOLUME: Int = 65
        const val DEFAULT_EFFECTS_VOLUME: Int = 85
    }
}
