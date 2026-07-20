package com.msa.compose_kmm.data

import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameSnapshotCodec
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.SaveDurability
import java.util.prefs.Preferences

class DesktopGameStateStore(
    nodeName: String = DEFAULT_PREFERENCES_NODE
) : GameStateStore {
    private val preferences = Preferences.userRoot().node(nodeName)

    override fun load(): GameSnapshot? = GameSnapshotCodec.decode(
        preferences.get(KEY_SNAPSHOT, null)
    )

    override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean = runCatching {
        preferences.put(KEY_SNAPSHOT, GameSnapshotCodec.encode(snapshot))
        if (durability == SaveDurability.Immediate) preferences.flush()
        true
    }.getOrDefault(false)

    override fun clear(durability: SaveDurability): Boolean = runCatching {
        preferences.remove(KEY_SNAPSHOT)
        if (durability == SaveDurability.Immediate) preferences.flush()
        true
    }.getOrDefault(false)

    private companion object {
        const val DEFAULT_PREFERENCES_NODE = "com/msa/bee"
        const val KEY_SNAPSHOT = "game_snapshot"
    }
}
