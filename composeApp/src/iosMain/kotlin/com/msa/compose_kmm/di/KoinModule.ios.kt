package com.msa.compose_kmm.di

import com.msa.compose_kmm.data.IosGameSettingsStore
import com.msa.compose_kmm.data.IosGameStateStore
import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.domain.IosAudioPlayer
import com.msa.compose_kmm.platform.AccessibilityPreferences
import com.msa.compose_kmm.platform.IosAccessibilityPreferences
import org.koin.dsl.module

actual val targetModule = module {
    factory<AudioPlayer> { IosAudioPlayer() }
    single<GameStateStore> { IosGameStateStore() }
    single<GameSettingsStore> { IosGameSettingsStore() }
    single<AccessibilityPreferences> { IosAccessibilityPreferences() }
}
