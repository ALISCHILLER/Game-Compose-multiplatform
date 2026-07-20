package com.msa.compose_kmm.platform

/**
 * Desktop API مشترکی برای Reduced Motion ندارد؛ یک System Property مستند برای
 * محیط‌های سازمانی، تست و اجرای سفارشی پشتیبانی می‌شود.
 */
class DesktopAccessibilityPreferences : AccessibilityPreferences {
    override fun prefersReducedMotion(): Boolean =
        System.getProperty(PROPERTY_NAME)?.equals("true", ignoreCase = true) == true

    private companion object {
        const val PROPERTY_NAME = "msa.bee.reduceMotion"
    }
}
