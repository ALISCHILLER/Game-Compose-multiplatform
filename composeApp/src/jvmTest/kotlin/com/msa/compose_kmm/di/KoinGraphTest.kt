package com.msa.compose_kmm.di

import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.GameSettingsStore
import com.msa.compose_kmm.domain.GameStateStore
import com.msa.compose_kmm.platform.AccessibilityPreferences
import com.msa.compose_kmm.presentation.GameController
import com.msa.compose_kmm.presentation.SettingsController
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.dsl.koinApplication

class KoinGraphTest {
    @Test
    fun desktopDependencyGraphResolvesAllApplicationServices() {
        val application = koinApplication {
            modules(sharedModule, targetModule)
        }

        try {
            assertNotNull(application.koin.get<GameController>())
            assertNotNull(application.koin.get<GameStateStore>())
            assertNotNull(application.koin.get<GameSettingsStore>())
            assertNotNull(application.koin.get<SettingsController>())
            assertNotNull(application.koin.get<AccessibilityPreferences>())
            application.koin.get<AudioPlayer>().release()
        } finally {
            application.close()
        }
    }
}
