package com.msa.compose_kmm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.msa.compose_kmm.domain.GameConfig
import kotlin.math.abs

/** Layered, fully programmatic scene with restrained parallax and no external art asset. */
@Composable
fun GameBackground(
    scrollOffset: Float,
    reduceMotion: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(GameSkyTop, GameSkyMiddle, GameSkyBottom),
                startY = 0f,
                endY = size.height
            )
        )

        val viewport = calculateGameViewport(
            canvasWidth = size.width,
            canvasHeight = size.height,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )
        val motionOffset = if (reduceMotion) 0f else scrollOffset

        withTransform({
            translate(viewport.offsetX, viewport.offsetY)
            scale(viewport.scale, viewport.scale, Offset.Zero)
        }) {
            drawSun()

            val cloudShift = positiveModulo(motionOffset * 0.035f, GameConfig.WORLD_WIDTH + 130f)
            drawCloud(center = Offset(72f - cloudShift, 100f), scale = 0.82f, alpha = 0.76f)
            drawCloud(
                center = Offset(292f - positiveModulo(cloudShift * 0.72f, 430f), 160f),
                scale = 0.6f,
                alpha = 0.62f
            )
            drawCloud(
                center = Offset(210f - positiveModulo(cloudShift * 0.45f, 500f), 58f),
                scale = 0.43f,
                alpha = 0.5f
            )

            drawHillLayer(
                baseY = 470f,
                color = GameHillFar,
                phase = positiveModulo(motionOffset * 0.025f, 180f),
                amplitude = 45f
            )
            drawHillLayer(
                baseY = 520f,
                color = GameHillNear,
                phase = positiveModulo(motionOffset * 0.055f, 220f),
                amplitude = 58f
            )
            drawShrubLine(
                baseY = GameConfig.WORLD_HEIGHT - GameConfig.GROUND_HEIGHT,
                phase = positiveModulo(motionOffset * 0.1f, 32f)
            )

            drawGround(motionOffset)
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.09f)),
                startY = size.height * 0.42f,
                endY = size.height
            )
        )
    }
}

private fun DrawScope.drawSun() {
    val center = Offset(286f, 92f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GameSunHalo, Color.Transparent),
            center = center,
            radius = 66f
        ),
        radius = 66f,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GameHoneyLight, GameSun),
            center = Offset(center.x - 8f, center.y - 9f),
            radius = 34f
        ),
        radius = 34f,
        center = center
    )
}

private fun DrawScope.drawHillLayer(
    baseY: Float,
    color: Color,
    phase: Float,
    amplitude: Float
) {
    val path = Path().apply {
        moveTo(-120f, GameConfig.WORLD_HEIGHT)
        lineTo(-120f, baseY)
        var x = -120f - phase
        while (x < GameConfig.WORLD_WIDTH + 180f) {
            quadraticBezierTo(
                x + 45f,
                baseY - amplitude,
                x + 90f,
                baseY
            )
            quadraticBezierTo(
                x + 135f,
                baseY + amplitude * 0.18f,
                x + 180f,
                baseY
            )
            x += 180f
        }
        lineTo(GameConfig.WORLD_WIDTH + 180f, GameConfig.WORLD_HEIGHT)
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawShrubLine(baseY: Float, phase: Float) {
    var x = -28f - phase
    while (x < GameConfig.WORLD_WIDTH + 28f) {
        drawCircle(
            color = GameHillShadow,
            radius = 15f,
            center = Offset(x, baseY + 1f)
        )
        drawCircle(
            color = GameGrass,
            radius = 11f,
            center = Offset(x + 13f, baseY + 2f)
        )
        x += 32f
    }
}

private fun DrawScope.drawGround(scrollOffset: Float) {
    val groundTop = GameConfig.WORLD_HEIGHT - GameConfig.GROUND_HEIGHT
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(GameGroundTop, GameGroundMiddle, GameGroundBottom),
            startY = groundTop,
            endY = GameConfig.WORLD_HEIGHT
        ),
        topLeft = Offset(0f, groundTop),
        size = Size(GameConfig.WORLD_WIDTH, GameConfig.GROUND_HEIGHT)
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(GameHoneyLight, GameHoneyYellow, GameGoldDeep),
            startY = groundTop,
            endY = groundTop + 11f
        ),
        topLeft = Offset(0f, groundTop),
        size = Size(GameConfig.WORLD_WIDTH, 11f)
    )

    val tileWidth = 38f
    var tileX = -tileWidth - positiveModulo(scrollOffset, tileWidth)
    var index = 0
    while (tileX < GameConfig.WORLD_WIDTH + tileWidth) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.13f),
            topLeft = Offset(tileX, groundTop + 25f + (index % 2) * 4f),
            size = Size(23f, 11f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = GameBeeBrown.copy(alpha = 0.17f),
            topLeft = Offset(tileX + 17f, groundTop + 57f - (index % 3) * 3f),
            size = Size(21f, 11f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        if (index % 4 == 0) {
            drawCircle(
                color = GameHoneyLight.copy(alpha = 0.5f),
                radius = 2.8f,
                center = Offset(tileX + 9f, groundTop + 17f)
            )
        }
        tileX += tileWidth
        index += 1
    }
}

private fun DrawScope.drawCloud(
    center: Offset,
    scale: Float,
    alpha: Float
) {
    val cloudColor = Color.White.copy(alpha = alpha)
    drawCircle(cloudColor, radius = 25f * scale, center = center)
    drawCircle(
        cloudColor,
        radius = 20f * scale,
        center = Offset(center.x - 24f * scale, center.y + 7f * scale)
    )
    drawCircle(
        cloudColor,
        radius = 17f * scale,
        center = Offset(center.x + 27f * scale, center.y + 9f * scale)
    )
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(center.x - 45f * scale, center.y + 4f * scale),
        size = Size(90f * scale, 28f * scale),
        cornerRadius = CornerRadius(16f * scale, 16f * scale)
    )
}

private fun positiveModulo(value: Float, divisor: Float): Float {
    if (divisor == 0f) return 0f
    val remainder = value % divisor
    return if (remainder < 0f) remainder + abs(divisor) else remainder
}
