package com.msa.compose_kmm

import com.msa.compose_kmm.data.DesktopGameStateStore
import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.SaveDurability
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopGameStateStoreTest {
    @Test
    fun desktopPreferencesRoundTripACompleteSnapshot() {
        val store = DesktopGameStateStore("com/msa/bee/test/${System.nanoTime()}")
        store.clear()
        val game = Game(Random(9)).apply {
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
