package com.msa.compose_kmm

import com.msa.compose_kmm.data.IosGameSettingsStore
import com.msa.compose_kmm.domain.GameSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IosGameSettingsStoreTest {
    @Test
    fun userDefaultsRoundTripSettings() {
        val store = IosGameSettingsStore()
        store.clear()
        val settings = GameSettings(musicVolume = 44, effectsEnabled = false)

        assertTrue(store.save(settings))
        assertEquals(settings, store.load())
        assertTrue(store.clear())
        assertNull(store.load())
    }
}
