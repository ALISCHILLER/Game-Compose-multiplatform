package com.msa.compose_kmm

import android.app.Application
import com.msa.compose_kmm.di.initializeKoin
import org.koin.android.ext.koin.androidContext

/**
 * Application اصلی Android.
 *
 * Koin باید قبل از نمایش Compose UI راه‌اندازی شود.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializeKoin {
            androidContext(this@MyApplication)
        }
    }
}