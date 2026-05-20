package com.msa.compose_kmm

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose-Kmm",
    ) {
        App()
    }
}