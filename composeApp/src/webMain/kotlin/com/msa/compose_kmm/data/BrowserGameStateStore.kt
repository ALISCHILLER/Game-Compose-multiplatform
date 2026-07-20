package com.msa.compose_kmm.data

import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameSnapshotCodec
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.SaveDurability
import kotlinx.browser.localStorage

class BrowserGameStateStore : GameStateStore {
    override fun load(): GameSnapshot? = runCatching {
        GameSnapshotCodec.decode(localStorage.getItem(KEY_SNAPSHOT))
    }.getOrNull()

    override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean = runCatching {
        localStorage.setItem(KEY_SNAPSHOT, GameSnapshotCodec.encode(snapshot))
        true
    }.getOrDefault(false)

    override fun clear(durability: SaveDurability): Boolean = runCatching {
        localStorage.removeItem(KEY_SNAPSHOT)
        true
    }.getOrDefault(false)

    private companion object {
        const val KEY_SNAPSHOT = "msa_bee_game_snapshot"
    }
}
