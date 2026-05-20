package com.msa.compose_kmm.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * ماژول مشترک برنامه.
 *
 * وابستگی‌هایی که بین همه پلتفرم‌ها مشترک هستند اینجا ثبت می‌شوند.
 */
val sharedModule = module {
    // وابستگی‌های مشترک آینده اینجا ثبت می‌شوند.
}

/**
 * ماژول مخصوص هر پلتفرم.
 *
 * پیاده‌سازی actual این مقدار باید در source set هر پلتفرم قرار بگیرد.
 */
expect val targetModule: Module

/**
 * راه‌اندازی Koin برای کل برنامه.
 */
fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)

        modules(
            sharedModule,
            targetModule
        )
    }
}