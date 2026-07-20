package com.msa.compose_kmm.di

import com.msa.compose_kmm.data.BrowserGameSettingsStore
import com.msa.compose_kmm.data.BrowserGameStateStore
import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.BrowserAudioPlayer
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.platform.AccessibilityPreferences
import com.msa.compose_kmm.platform.BrowserAccessibilityPreferences
import org.koin.dsl.module

actual val targetModule = module {
    factory<AudioPlayer> { BrowserAudioPlayer() }
    single<GameStateStore> { BrowserGameStateStore() }
    single<GameSettingsStore> { BrowserGameSettingsStore() }
    single<AccessibilityPreferences> { BrowserAccessibilityPreferences() }
}
