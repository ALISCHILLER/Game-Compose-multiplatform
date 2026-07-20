package com.msa.compose_kmm

import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsCodec
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.presentation.SettingsController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameSettingsTest {
    @Test
    fun settingsCodecRoundTripsEveryPreference() {
        val settings = GameSettings(
            soundEnabled = false,
            musicEnabled = false,
            effectsEnabled = true,
            musicVolume = 34,
            effectsVolume = 72,
            reduceMotion = true,
            showGameplayHints = false
        )

        assertEquals(settings, GameSettingsCodec.decode(GameSettingsCodec.encode(settings)))
    }

    @Test
    fun settingsCodecRejectsTampering() {
        val encoded = GameSettingsCodec.encode(GameSettings())
        val fields = encoded.split('|').toMutableList()
        fields[4] = "100"

        assertNull(GameSettingsCodec.decode(fields.joinToString("|")))
    }

    @Test
    fun masterSoundControlsEffectiveVolumes() {
        val muted = GameSettings(soundEnabled = false, musicVolume = 90, effectsVolume = 80)
        val enabled = muted.copy(soundEnabled = true)

        assertEquals(0f, muted.effectiveMusicVolume)
        assertEquals(0f, muted.effectiveEffectsVolume)
        assertEquals(0.9f, enabled.effectiveMusicVolume)
        assertEquals(0.8f, enabled.effectiveEffectsVolume)
    }

    @Test
    fun settingsControllerPersistsChangesAndRestoresDefaults() {
        val store = InMemorySettingsStore()
        val controller = SettingsController(store)

        assertTrue(controller.setSoundEnabled(false))
        assertTrue(controller.setMusicVolume(41))
        assertTrue(controller.setReduceMotion(true))
        assertFalse(controller.state.value.soundEnabled)
        assertEquals(41, controller.state.value.musicVolume)
        assertTrue(controller.state.value.reduceMotion)
        assertEquals(controller.state.value, store.settings)

        assertTrue(controller.restoreDefaults())
        assertEquals(GameSettings(), controller.state.value)
        assertEquals(GameSettings(), store.settings)
    }

    @Test
    fun settingsControllerContainsStorageFailures() {
        val controller = SettingsController(FailingSettingsStore())

        assertFalse(controller.setEffectsEnabled(false))
        assertFalse(controller.state.value.effectsEnabled)
    }

    private class InMemorySettingsStore : GameSettingsStore {
        var settings: GameSettings? = null

        override fun load(): GameSettings? = settings
        override fun save(settings: GameSettings): Boolean {
            this.settings = settings
            return true
        }
        override fun clear(): Boolean {
            settings = null
            return true
        }
    }

    private class FailingSettingsStore : GameSettingsStore {
        override fun load(): GameSettings? = throw IllegalStateException("load failure")
        override fun save(settings: GameSettings): Boolean = throw IllegalStateException("save failure")
        override fun clear(): Boolean = throw IllegalStateException("clear failure")
    }
}
