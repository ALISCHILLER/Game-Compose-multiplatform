package com.msa.compose_kmm.data

import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsCodec
import com.msa.compose_kmm.domain.GameSettingsStore
import platform.Foundation.NSUserDefaults

class IosGameSettingsStore : GameSettingsStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun load(): GameSettings? = runCatching {
        GameSettingsCodec.decode(defaults.stringForKey(KEY_SETTINGS))
    }.getOrNull()

    override fun save(settings: GameSettings): Boolean = runCatching {
        defaults.setObject(GameSettingsCodec.encode(settings), forKey = KEY_SETTINGS)
        defaults.synchronize()
    }.getOrDefault(false)

    override fun clear(): Boolean = runCatching {
        defaults.removeObjectForKey(KEY_SETTINGS)
        defaults.synchronize()
    }.getOrDefault(false)

    private companion object {
        const val KEY_SETTINGS = "msa_bee_game_settings"
    }
}
