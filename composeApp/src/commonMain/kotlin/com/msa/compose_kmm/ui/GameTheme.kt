package com.msa.compose_kmm.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * تم مرکزی بازی.
 */
@Composable
fun ZarBeeGameTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = GamePrimaryOrange,
        secondary = GameHoneyYellow,
        tertiary = GameLeafGreen,
        background = GameCardDark,
        surface = GamePanel,
        onPrimary = GameTextWhite,
        onSecondary = GameCardDark,
        onBackground = GameTextWhite,
        onSurface = GameTextWhite
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}