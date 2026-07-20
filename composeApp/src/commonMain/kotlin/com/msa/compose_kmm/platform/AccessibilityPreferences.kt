package com.msa.compose_kmm.platform

/** تنظیمات Accessibility که رفتار تزئینی UI را با ترجیح سیستم هماهنگ می‌کند. */
interface AccessibilityPreferences {
    fun prefersReducedMotion(): Boolean
}
