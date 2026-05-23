package com.msa.compose_kmm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.msa.compose_kmm.di.initializeKoin

/**
 * Entry point نسخه Web/Wasm.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeKoin()

    ComposeViewport {
        App()
    }
}