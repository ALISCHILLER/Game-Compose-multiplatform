package com.msa.compose_kmm.ui

import kotlin.math.min

/** تبدیل فضای منطقی ثابت بازی به اندازه‌ی واقعی Canvas. */
data class GameViewport(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
)

fun calculateGameViewport(
    canvasWidth: Float,
    canvasHeight: Float,
    worldWidth: Float,
    worldHeight: Float
): GameViewport {
    if (canvasWidth <= 0f || canvasHeight <= 0f || worldWidth <= 0f || worldHeight <= 0f) {
        return GameViewport(scale = 1f, offsetX = 0f, offsetY = 0f)
    }

    val widthFitScale = canvasWidth / worldWidth
    val heightFitScale = canvasHeight / worldHeight
    val fitScale = min(widthFitScale, heightFitScale)
    val aspectRatio = canvasWidth / canvasHeight

    // در Landscape کم‌ارتفاع، Fit کامل باعث می‌شود زمین بازی بسیار باریک و شخصیت بیش‌ازحد کوچک شود.
    // با Zoom محدود و اتصال زمین به پایین صفحه، بخش کم‌اهمیت بالای World Crop می‌شود، بدون کشیدگی تصویر.
    val landscapeBoost = when {
        aspectRatio >= 2.1f -> 1.38f
        aspectRatio >= 1.65f -> 1.25f
        else -> 1f
    }
    val scale = if (landscapeBoost > 1f) {
        min(widthFitScale, fitScale * landscapeBoost)
    } else {
        fitScale
    }

    val renderedWidth = worldWidth * scale
    val renderedHeight = worldHeight * scale

    return GameViewport(
        scale = scale,
        offsetX = (canvasWidth - renderedWidth) / 2f,
        // زمین همیشه به پایین Canvas متصل است؛ در Landscape ممکن است offset منفی و Top Crop شود.
        offsetY = canvasHeight - renderedHeight
    )
}
