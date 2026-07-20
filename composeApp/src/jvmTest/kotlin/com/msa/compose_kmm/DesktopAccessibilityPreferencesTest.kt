package com.msa.compose_kmm

import com.msa.compose_kmm.platform.DesktopAccessibilityPreferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopAccessibilityPreferencesTest {
    @AfterTest
    fun cleanup() {
        System.clearProperty(PROPERTY_NAME)
    }

    @Test
    fun defaultsToMotionEnabled() {
        System.clearProperty(PROPERTY_NAME)
        assertFalse(DesktopAccessibilityPreferences().prefersReducedMotion())
    }

    @Test
    fun readsDocumentedReducedMotionOverride() {
        System.setProperty(PROPERTY_NAME, "true")
        assertTrue(DesktopAccessibilityPreferences().prefersReducedMotion())
    }

    private companion object {
        const val PROPERTY_NAME = "msa.bee.reduceMotion"
    }
}
