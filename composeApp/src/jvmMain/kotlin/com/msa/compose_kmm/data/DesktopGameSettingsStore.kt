package com.msa.compose_kmm.data

import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsCodec
import com.msa.compose_kmm.domain.GameSettingsStore
import java.util.prefs.Preferences

class DesktopGameSettingsStore(
    nodeName: String = DEFAULT_PREFERENCES_NODE
) : GameSettingsStore {
    private val preferences = Preferences.userRoot().node(nodeName)

    override fun load(): GameSettings? = GameSettingsCodec.decode(
        preferences.get(KEY_SETTINGS, null)
    )

    override fun save(settings: GameSettings): Boolean = runCatching {
        preferences.put(KEY_SETTINGS, GameSettingsCodec.encode(settings))
        preferences.flush()
        true
    }.getOrDefault(false)

    override fun clear(): Boolean = runCatching {
        preferences.remove(KEY_SETTINGS)
        preferences.flush()
        true
    }.getOrDefault(false)

    private companion object {
        const val DEFAULT_PREFERENCES_NODE = "com/msa/bee"
        const val KEY_SETTINGS = "game_settings"
    }
}
