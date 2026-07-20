package com.msa.compose_kmm.di

import com.msa.compose_kmm.presentation.GameController
import com.msa.compose_kmm.presentation.SettingsController
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    factory { GameController(store = get()) }
    single { SettingsController(store = get()) }
}

expect val targetModule: Module

private var koinInitialized = false

/** Koin باید قبل از Composition و فقط یک‌بار در هر Process راه‌اندازی شود. */
fun initializeKoin(config: (KoinApplication.() -> Unit)? = null) {
    if (koinInitialized) return

    koinInitialized = true
    try {
        startKoin {
            config?.invoke(this)
            modules(sharedModule, targetModule)
        }
    } catch (error: Throwable) {
        koinInitialized = false
        throw error
    }
}
