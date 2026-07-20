package com.msa.compose_kmm

import androidx.compose.ui.window.ComposeUIViewController
import com.msa.compose_kmm.di.initializeKoin

/** Entry point نسخه iOS؛ DI قبل از ایجاد Composition راه‌اندازی می‌شود. */
fun MainViewController() = run {
    initializeKoin()
    ComposeUIViewController { App() }
}
