package com.msa.compose_kmm

import com.msa.compose_kmm.data.DesktopGameSettingsStore
import com.msa.compose_kmm.domain.GameSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopGameSettingsStoreTest {
    @Test
    fun desktopPreferencesRoundTripSettings() {
        val store = DesktopGameSettingsStore("com/msa/bee/settings-test/${System.nanoTime()}")
        store.clear()
        val settings = GameSettings(
            soundEnabled = false,
            musicVolume = 33,
            effectsVolume = 77,
            reduceMotion = true,
            showGameplayHints = false
        )

        assertTrue(store.save(settings))
        assertEquals(settings, store.load())
        assertTrue(store.clear())
        assertNull(store.load())
    }
}
