package com.msa.compose_kmm

import com.msa.compose_kmm.data.BrowserGameSettingsStore
import com.msa.compose_kmm.domain.GameSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrowserGameSettingsStoreWasmTest {
    @Test
    fun localStorageRoundTripsSettings() {
        val store = BrowserGameSettingsStore()
        store.clear()
        val settings = GameSettings(effectsVolume = 58, showGameplayHints = false)

        assertTrue(store.save(settings))
        assertEquals(settings, store.load())
        assertTrue(store.clear())
        assertNull(store.load())
    }
}
