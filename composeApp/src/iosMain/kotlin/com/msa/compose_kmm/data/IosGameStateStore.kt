package com.msa.compose_kmm.data

import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameSnapshotCodec
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.SaveDurability
import platform.Foundation.NSUserDefaults

class IosGameStateStore : GameStateStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun load(): GameSnapshot? = runCatching {
        GameSnapshotCodec.decode(defaults.stringForKey(KEY_SNAPSHOT))
    }.getOrNull()

    override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean = runCatching {
        defaults.setObject(GameSnapshotCodec.encode(snapshot), forKey = KEY_SNAPSHOT)
        durability != SaveDurability.Immediate || defaults.synchronize()
    }.getOrDefault(false)

    override fun clear(durability: SaveDurability): Boolean = runCatching {
        defaults.removeObjectForKey(KEY_SNAPSHOT)
        durability != SaveDurability.Immediate || defaults.synchronize()
    }.getOrDefault(false)

    private companion object {
        const val KEY_SNAPSHOT = "msa_bee_game_snapshot"
    }
}
