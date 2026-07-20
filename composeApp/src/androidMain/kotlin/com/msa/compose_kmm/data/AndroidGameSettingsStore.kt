package com.msa.compose_kmm.data

import android.content.Context
import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsCodec
import com.msa.compose_kmm.domain.GameSettingsStore

class AndroidGameSettingsStore(context: Context) : GameSettingsStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(): GameSettings? = runCatching {
        GameSettingsCodec.decode(preferences.getString(KEY_SETTINGS, null))
    }.getOrNull()

    override fun save(settings: GameSettings): Boolean = runCatching {
        preferences.edit()
            .putString(KEY_SETTINGS, GameSettingsCodec.encode(settings))
            .commit()
    }.getOrDefault(false)

    override fun clear(): Boolean = runCatching {
        preferences.edit().remove(KEY_SETTINGS).commit()
    }.getOrDefault(false)

    private companion object {
        const val PREFERENCES_NAME = "msa_bee_settings"
        const val KEY_SETTINGS = "game_settings"
    }
}
