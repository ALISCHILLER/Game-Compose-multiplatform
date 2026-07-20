package com.msa.audit

import com.msa.compose_kmm.domain.GameSettings
import com.msa.compose_kmm.domain.GameSettingsCodec
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.presentation.SettingsController

private class AuditSettingsStore : GameSettingsStore {
    var saved: GameSettings? = null
    var saves: Int = 0

    override fun load(): GameSettings? = saved

    override fun save(settings: GameSettings): Boolean {
        saved = settings
        saves += 1
        return true
    }

    override fun clear(): Boolean {
        saved = null
        return true
    }
}

fun main() {
    val codecSample = GameSettings(
        soundEnabled = true,
        musicEnabled = false,
        effectsEnabled = true,
        musicVolume = 37,
        effectsVolume = 82,
        reduceMotion = true,
        showGameplayHints = false
    )
    check(GameSettingsCodec.decode(GameSettingsCodec.encode(codecSample)) == codecSample)

    val store = AuditSettingsStore()
    val controller = SettingsController(store)
    check(controller.state.value == GameSettings())
    check(controller.setSoundEnabled(false))
    check(controller.setMusicVolume(42))
    check(controller.setEffectsVolume(73))
    check(controller.setReduceMotion(true))
    check(controller.setShowGameplayHints(false))

    val persisted = checkNotNull(store.saved)
    check(!persisted.soundEnabled)
    check(persisted.musicVolume == 42)
    check(persisted.effectsVolume == 73)
    check(persisted.reduceMotion)
    check(!persisted.showGameplayHints)
    check(persisted.effectiveMusicVolume == 0f)
    check(persisted.effectiveEffectsVolume == 0f)

    check(controller.restoreDefaults())
    check(controller.state.value == GameSettings())

    println("settings-runtime-audit-ok saves=${store.saves} schema=${GameSettings.CURRENT_SCHEMA_VERSION}")
}
