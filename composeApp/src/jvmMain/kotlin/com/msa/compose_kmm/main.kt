package com.msa.compose_kmm

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.msa.compose_kmm.di.initializeKoin

/** Entry point نسخه Desktop. */
fun main() = application {
    initializeKoin ()

    Window(
        onCloseRequest = ::exitApplication,
        title = "زنبور زرنگ"
    ) {
        App()
    }
}