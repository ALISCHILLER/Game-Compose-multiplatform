package com.msa.compose_kmm

import java.awt.EventQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopMainDispatcherTest {
    @Test
    fun swingArtifactInstallsMainDispatcherOnAwtEventThread() = runBlocking {
        val isEventDispatchThread = withContext(Dispatchers.Main) {
            EventQueue.isDispatchThread()
        }

        assertTrue(
            isEventDispatchThread,
            "Dispatchers.Main must be backed by the Swing/AWT event-dispatch thread"
        )
    }
}
