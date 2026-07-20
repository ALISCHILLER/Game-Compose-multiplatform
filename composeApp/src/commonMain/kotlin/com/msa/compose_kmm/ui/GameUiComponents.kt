package com.msa.compose_kmm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class BeeMood {
    Happy,
    Proud,
    Tired
}

/** Decorative full-screen overlay with a subtle honeycomb identity. */
@Composable
fun OverlayBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameOverlayTop, GameOverlayBottom)
                )
            )
            .clearAndSetSemantics { }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = 22f
            val horizontalStep = radius * 3f
            val verticalStep = radius * 1.72f
            var row = -1
            var y = -radius
            while (y < size.height + radius) {
                val offset = if (row % 2 == 0) 0f else horizontalStep / 2f
                var x = -horizontalStep + offset
                while (x < size.width + horizontalStep) {
                    drawHexagon(
                        center = Offset(x, y),
                        radius = radius,
                        color = GameHoneyLight.copy(alpha = 0.055f)
                    )
                    x += horizontalStep
                }
                y += verticalStep
                row += 1
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GameHoneyYellow.copy(alpha = 0.12f),
                        GameHoneyYellow.copy(alpha = 0f)
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.18f),
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.18f, size.height * 0.18f)
            )
        }
    }
}

/** Elevated panel shared by start and game-over experiences. */
@Composable
fun GamePanelSurface(
    modifier: Modifier,
    cornerRadius: Dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = GamePanel,
        contentColor = GameTextWhite,
        shadowElevation = 18.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, GamePanelBorderSoft)
    ) {
        content()
    }
}

/** Primary game action with consistent focus, elevation and touch target. */
@Composable
fun GamePrimaryButton(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .heightIn(min = 58.dp)
            .widthIn(min = 160.dp),
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 16.dp else 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GamePrimaryOrange,
            contentColor = GameTextWhite,
            disabledContainerColor = GamePrimaryOrange.copy(alpha = 0.45f),
            disabledContentColor = GameTextWhite.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 7.dp,
            pressedElevation = 2.dp,
            focusedElevation = 10.dp
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = if (compact) 18.dp else 24.dp,
            vertical = 12.dp
        )
    ) {
        Text(
            text = label,
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Secondary action used for settings and non-destructive utility actions. */
@Composable
fun GameSecondaryButton(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .heightIn(min = 50.dp)
            .widthIn(min = 128.dp),
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 15.dp else 18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = GameHoneyLight
        ),
        border = BorderStroke(1.dp, GamePanelBorder),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = if (compact) 15.dp else 20.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Small visual label used for mode and achievement badges. */
@Composable
fun GameBadge(
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (emphasized) GameHoneyYellow else GamePanelSoft,
        contentColor = if (emphasized) GameBeeBrownDark else GameHoneyLight,
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasized) GameHoneyLight else GamePanelBorderSoft
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A compact instruction chip that remains legible at large font scale. */
@Composable
fun ControlHintPill(
    title: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = GameCardDark,
        contentColor = GameTextWhite,
        border = BorderStroke(1.dp, GamePanelBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = GameHoneyYellow,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                color = GameTextMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Reusable inline metric for the start screen and compact results. */
@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = GameCardDark,
        contentColor = GameTextWhite,
        border = BorderStroke(1.dp, GamePanelBorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                color = GameTextMuted,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = GameHoneyYellow,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

/** Programmatic mascot used by overlays; no external image or font asset is required. */
@Composable
fun BeeEmblem(
    modifier: Modifier = Modifier,
    size: Dp,
    mood: BeeMood,
    tiltDegrees: Float = 0f
) {
    Canvas(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { }
            .graphicsLayer {
                rotationZ = tiltDegrees
            }
    ) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.27f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GameHoneyYellow.copy(alpha = 0.28f),
                    GameHoneyYellow.copy(alpha = 0f)
                ),
                center = center,
                radius = this.size.minDimension * 0.48f
            ),
            radius = this.size.minDimension * 0.48f,
            center = center
        )

        drawMascotBee(center = center, radius = radius, mood = mood)
    }
}

private fun DrawScope.drawMascotBee(center: Offset, radius: Float, mood: BeeMood) {
    val wingLift = if (mood == BeeMood.Tired) -radius * 0.42f else -radius * 0.76f

    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(GameBeeWing, GameBeeWing.copy(alpha = 0.45f)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x, center.y)
        ),
        topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 1.15f + wingLift),
        size = Size(radius * 1.35f, radius * 1.2f)
    )
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(GameBeeWing, GameBeeWing.copy(alpha = 0.45f)),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x + radius, center.y)
        ),
        topLeft = Offset(center.x - radius * 0.1f, center.y - radius * 1.18f + wingLift),
        size = Size(radius * 1.35f, radius * 1.2f)
    )

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(GameBeeYellowLight, GameBeeYellow, ColorMixBeeGold),
            startX = center.x - radius,
            endX = center.x + radius
        ),
        topLeft = Offset(center.x - radius, center.y - radius * 0.72f),
        size = Size(radius * 2f, radius * 1.44f)
    )

    repeat(3) { index ->
        drawRoundRect(
            color = GameBeeBrown,
            topLeft = Offset(
                center.x - radius * 0.46f + index * radius * 0.42f,
                center.y - radius * 0.66f
            ),
            size = Size(radius * 0.25f, radius * 1.32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.12f)
        )
    }

    val headCenter = Offset(center.x + radius * 0.78f, center.y - radius * 0.02f)
    drawCircle(GameBeeBrown, radius * 0.55f, headCenter)

    drawLine(
        color = GameBeeBrownDark,
        start = Offset(headCenter.x - radius * 0.08f, headCenter.y - radius * 0.48f),
        end = Offset(headCenter.x - radius * 0.22f, headCenter.y - radius * 0.88f),
        strokeWidth = radius * 0.09f
    )
    drawLine(
        color = GameBeeBrownDark,
        start = Offset(headCenter.x + radius * 0.15f, headCenter.y - radius * 0.47f),
        end = Offset(headCenter.x + radius * 0.28f, headCenter.y - radius * 0.86f),
        strokeWidth = radius * 0.09f
    )
    drawCircle(GameHoneyYellow, radius * 0.09f, Offset(headCenter.x - radius * 0.24f, headCenter.y - radius * 0.9f))
    drawCircle(GameHoneyYellow, radius * 0.09f, Offset(headCenter.x + radius * 0.3f, headCenter.y - radius * 0.88f))

    when (mood) {
        BeeMood.Happy,
        BeeMood.Proud -> {
            drawCircle(ColorWhite, radius * 0.18f, Offset(headCenter.x + radius * 0.13f, headCenter.y - radius * 0.15f))
            drawCircle(GameBeeBrownDark, radius * 0.08f, Offset(headCenter.x + radius * 0.17f, headCenter.y - radius * 0.15f))
            drawCircle(GameBeeCheek, radius * 0.1f, Offset(headCenter.x + radius * 0.34f, headCenter.y + radius * 0.12f))
            drawArc(
                color = GameBeeBrownDark,
                startAngle = 18f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = Offset(headCenter.x + radius * 0.02f, headCenter.y - radius * 0.02f),
                size = Size(radius * 0.42f, radius * 0.35f),
                style = Stroke(width = radius * 0.075f)
            )
        }
        BeeMood.Tired -> {
            drawLine(
                color = GameTextWhite,
                start = Offset(headCenter.x + radius * 0.02f, headCenter.y - radius * 0.12f),
                end = Offset(headCenter.x + radius * 0.31f, headCenter.y - radius * 0.12f),
                strokeWidth = radius * 0.09f
            )
            drawArc(
                color = GameBeeBrownDark,
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(headCenter.x + radius * 0.04f, headCenter.y + radius * 0.08f),
                size = Size(radius * 0.38f, radius * 0.3f),
                style = Stroke(width = radius * 0.075f)
            )
        }
    }

    val stinger = Path().apply {
        moveTo(center.x - radius * 1.02f, center.y - radius * 0.14f)
        lineTo(center.x - radius * 1.38f, center.y)
        lineTo(center.x - radius * 1.02f, center.y + radius * 0.14f)
        close()
    }
    drawPath(stinger, GameBeeBrownDark)

    if (mood == BeeMood.Proud) {
        drawCircle(
            color = GameHoneyLight.copy(alpha = 0.8f),
            radius = radius * 0.08f,
            center = Offset(center.x - radius * 1.22f, center.y - radius * 0.9f)
        )
        drawCircle(
            color = GameHoneyLight.copy(alpha = 0.65f),
            radius = radius * 0.055f,
            center = Offset(center.x + radius * 1.32f, center.y - radius * 0.95f)
        )
    }
}

private fun DrawScope.drawHexagon(center: Offset, radius: Float, color: androidx.compose.ui.graphics.Color) {
    val path = Path()
    repeat(6) { index ->
        val angle = (PI / 3.0 * index - PI / 6.0)
        val point = Offset(
            x = center.x + cos(angle).toFloat() * radius,
            y = center.y + sin(angle).toFloat() * radius
        )
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = 1.2f))
}

private val ColorWhite = androidx.compose.ui.graphics.Color.White
private val ColorMixBeeGold = androidx.compose.ui.graphics.Color(0xFFF1A90D)
