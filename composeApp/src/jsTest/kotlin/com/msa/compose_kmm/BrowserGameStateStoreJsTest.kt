package com.msa.compose_kmm

import com.msa.compose_kmm.data.BrowserGameStateStore
import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.SaveDurability
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrowserGameStateStoreJsTest {
    @Test
    fun localStorageRoundTripsACompleteSnapshot() {
        val store = BrowserGameStateStore()
        store.clear()
        val game = Game(Random(13)).apply {
            start()
            jump()
            repeat(20) { updateNanos(8_333_333L) }
        }

        assertTrue(store.save(game.snapshot(), SaveDurability.Immediate))

        assertEquals(game.snapshot(), store.load())
        assertTrue(store.clear(SaveDurability.Immediate))
        assertNull(store.load())
    }
}
