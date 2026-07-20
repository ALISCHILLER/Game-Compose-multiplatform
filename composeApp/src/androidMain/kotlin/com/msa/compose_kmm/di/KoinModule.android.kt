package com.msa.compose_kmm.di

import com.msa.compose_kmm.data.AndroidGameSettingsStore
import com.msa.compose_kmm.data.AndroidGameStateStore
import com.msa.compose_kmm.domain.AndroidAudioPlayer
import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.platform.AccessibilityPreferences
import com.msa.compose_kmm.platform.AndroidAccessibilityPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val targetModule = module {
    factory<AudioPlayer> { AndroidAudioPlayer(context = androidContext()) }
    single<GameStateStore> { AndroidGameStateStore(context = androidContext()) }
    single<GameSettingsStore> { AndroidGameSettingsStore(context = androidContext()) }
    single<AccessibilityPreferences> { AndroidAccessibilityPreferences(context = androidContext()) }
}
