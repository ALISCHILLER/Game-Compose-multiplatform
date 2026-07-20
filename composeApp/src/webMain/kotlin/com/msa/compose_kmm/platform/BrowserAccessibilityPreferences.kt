package com.msa.compose_kmm.platform

import kotlinx.browser.window

class BrowserAccessibilityPreferences : AccessibilityPreferences {
    override fun prefersReducedMotion(): Boolean =
        window.matchMedia("(prefers-reduced-motion: reduce)").matches
}
