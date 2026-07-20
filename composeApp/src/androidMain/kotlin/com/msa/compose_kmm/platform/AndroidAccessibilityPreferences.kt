package com.msa.compose_kmm.platform

import android.content.Context
import android.provider.Settings

class AndroidAccessibilityPreferences(context: Context) : AccessibilityPreferences {
    private val resolver = context.applicationContext.contentResolver

    override fun prefersReducedMotion(): Boolean = runCatching {
        val animatorScale = Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            DEFAULT_ANIMATION_SCALE
        )
        val transitionScale = Settings.Global.getFloat(
            resolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            DEFAULT_ANIMATION_SCALE
        )
        animatorScale == 0f || transitionScale == 0f
    }.getOrDefault(false)

    private companion object {
        const val DEFAULT_ANIMATION_SCALE = 1f
    }
}
