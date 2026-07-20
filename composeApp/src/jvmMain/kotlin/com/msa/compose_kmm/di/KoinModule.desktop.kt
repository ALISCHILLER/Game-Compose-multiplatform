package com.msa.compose_kmm.di

import com.msa.compose_kmm.data.DesktopGameSettingsStore
import com.msa.compose_kmm.data.DesktopGameStateStore
import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.DesktopAudioPlayer
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.platform.AccessibilityPreferences
import com.msa.compose_kmm.platform.DesktopAccessibilityPreferences
import org.koin.dsl.module

actual val targetModule = module {
    factory<AudioPlayer> { DesktopAudioPlayer() }
    single<GameStateStore> { DesktopGameStateStore() }
    single<GameSettingsStore> { DesktopGameSettingsStore() }
    single<AccessibilityPreferences> { DesktopAccessibilityPreferences() }
}
