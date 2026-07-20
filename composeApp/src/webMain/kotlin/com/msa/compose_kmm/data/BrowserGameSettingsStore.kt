package com.msa.compose_kmm.data

import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsCodec
import com.msa.compose_kmm.domain.GameSettingsStore
import kotlinx.browser.localStorage

class BrowserGameSettingsStore : GameSettingsStore {
    override fun load(): GameSettings? = runCatching {
        GameSettingsCodec.decode(localStorage.getItem(KEY_SETTINGS))
    }.getOrNull()

    override fun save(settings: GameSettings): Boolean = runCatching {
        localStorage.setItem(KEY_SETTINGS, GameSettingsCodec.encode(settings))
        true
    }.getOrDefault(false)

    override fun clear(): Boolean = runCatching {
        localStorage.removeItem(KEY_SETTINGS)
        true
    }.getOrDefault(false)

    private companion object {
        const val KEY_SETTINGS = "msa_bee_game_settings"
    }
}
