package com.msa.compose_kmm.platform

import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

class IosAccessibilityPreferences : AccessibilityPreferences {
    override fun prefersReducedMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
}
