package com.msa.compose_kmm.presentation

import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** State holder تنظیمات با ذخیره فوری و تحمل خطای Storage. */
class SettingsController(
    private val store: GameSettingsStore
) {
    private val _state = MutableStateFlow(loadInitialSettings())
    val state: StateFlow<GameSettings> = _state.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) = update { copy(soundEnabled = enabled) }
    fun setMusicEnabled(enabled: Boolean) = update { copy(musicEnabled = enabled) }
    fun setEffectsEnabled(enabled: Boolean) = update { copy(effectsEnabled = enabled) }
    fun setMusicVolume(volume: Int) = update { copy(musicVolume = volume) }
    fun setEffectsVolume(volume: Int) = update { copy(effectsVolume = volume) }
    fun setReduceMotion(enabled: Boolean) = update { copy(reduceMotion = enabled) }
    fun setShowGameplayHints(enabled: Boolean) = update { copy(showGameplayHints = enabled) }

    fun restoreDefaults(): Boolean {
        val defaults = GameSettings()
        _state.value = defaults
        return runCatching { store.save(defaults) }.getOrDefault(false)
    }

    private fun update(transform: GameSettings.() -> GameSettings): Boolean {
        val next = _state.value.transform().normalized()
        if (next == _state.value) return true
        _state.value = next
        return runCatching { store.save(next) }.getOrDefault(false)
    }

    private fun loadInitialSettings(): GameSettings {
        val loaded = runCatching { store.load() }.getOrNull()?.normalized()
        if (loaded != null) return loaded
        val defaults = GameSettings()
        runCatching { store.save(defaults) }
        return defaults
    }
}
