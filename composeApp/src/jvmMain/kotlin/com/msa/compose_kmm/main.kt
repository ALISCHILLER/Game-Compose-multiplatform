package com.msa.compose_kmm

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.msa.compose_kmm.di.initializeKoin

fun main() {
    initializeKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MSA Bee",
            state = WindowState(width = 520.dp, height = 820.dp)
        ) {
            App()
        }
    }
}
