package com.msa.compose_kmm.di

import com.msa.compose_kmm.domain.AudioPlayer
import org.koin.dsl.module

actual val targetModule = module {
    single<AudioPlayer> { AudioPlayer() }
}