package com.msa.compose_kmm.data

import android.content.Context
import com.msa.compose_kmm.domain.GameSnapshot
import com.msa.compose_kmm.domain.GameSnapshotCodec
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.SaveDurability

class AndroidGameStateStore(context: Context) : GameStateStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(): GameSnapshot? = runCatching {
        GameSnapshotCodec.decode(preferences.getString(KEY_SNAPSHOT, null))
    }.getOrNull()

    override fun save(snapshot: GameSnapshot, durability: SaveDurability): Boolean = runCatching {
        val editor = preferences.edit().putString(KEY_SNAPSHOT, GameSnapshotCodec.encode(snapshot))
        when (durability) {
            SaveDurability.Deferred -> {
                editor.apply()
                true
            }

            SaveDurability.Immediate -> editor.commit()
        }
    }.getOrDefault(false)

    override fun clear(durability: SaveDurability): Boolean = runCatching {
        val editor = preferences.edit().remove(KEY_SNAPSHOT)
        when (durability) {
            SaveDurability.Deferred -> {
                editor.apply()
                true
            }

            SaveDurability.Immediate -> editor.commit()
        }
    }.getOrDefault(false)

    private companion object {
        const val PREFERENCES_NAME = "msa_bee_state"
        const val KEY_SNAPSHOT = "game_snapshot"
    }
}
