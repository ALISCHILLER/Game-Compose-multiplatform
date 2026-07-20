package com.msa.compose_kmm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.msa.compose_kmm.domain.GameConfig
import com.msa.compose_kmm.domain.GameSnapshot
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.game_running_score
import compose_kmm.composeapp.generated.resources.jump_action
import org.jetbrains.compose.resources.stringResource

/** Scalable game canvas with touch, mouse, keyboard and accessible custom actions. */
@Composable
fun GameCanvas(
    state: GameSnapshot,
    wingFrame: Int,
    enabled: Boolean,
    reduceMotion: Boolean,
    onJump: () -> Unit
) {
    val jumpLabel = stringResource(Res.string.jump_action)
    val scoreValue = localizedNumber(state.score)
    val runningDescription = stringResource(Res.string.game_running_score, scoreValue)
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val beeAngle = when {
        state.beeVelocity > GameConfig.MAX_VERTICAL_VELOCITY * 0.62f -> 24f
        state.beeVelocity < 0f -> -18f
        else -> 5f
    }

    LaunchedEffect(enabled) {
        if (enabled) focusRequester.requestFocus()
    }

    val interactiveModifier = if (enabled) {
        Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                val isJumpKey = event.key == Key.Spacebar ||
                    event.key == Key.Enter ||
                    event.key == Key.DirectionUp
                if (event.type == KeyEventType.KeyDown && isJumpKey) {
                    onJump()
                    true
                } else {
                    false
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = jumpLabel
                stateDescription = runningDescription
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = jumpLabel,
                onClick = onJump
            )
    } else {
        Modifier.clearAndSetSemantics { }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(interactiveModifier)
            .testTag(UiTestTags.GAME_CANVAS)
    ) {
        val viewport = calculateGameViewport(
            canvasWidth = size.width,
            canvasHeight = size.height,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )

        withTransform({
            translate(viewport.offsetX, viewport.offsetY)
            scale(viewport.scale, viewport.scale, Offset.Zero)
        }) {
            state.pipePairs.forEach(::drawPipePair)

            val floorY = GameConfig.WORLD_HEIGHT - GameConfig.GROUND_HEIGHT
            val shadowWidth = state.bee.radius * 2.2f
            val distanceToGround = (floorY - state.bee.y).coerceAtLeast(0f)
            val shadowAlpha = (1f - distanceToGround / (GameConfig.WORLD_HEIGHT * 0.58f))
                .coerceIn(0.1f, 0.4f)

            drawOval(
                color = GameSoftShadow.copy(alpha = shadowAlpha),
                topLeft = Offset(state.bee.x - shadowWidth / 2f, floorY + 8f),
                size = Size(shadowWidth, state.bee.radius * 0.34f)
            )

            if (!reduceMotion && enabled) {
                drawBeeMotionTrail(
                    center = Offset(state.bee.x, state.bee.y),
                    radius = state.bee.radius,
                    velocity = state.beeVelocity
                )
            }

            rotate(beeAngle, pivot = Offset(state.bee.x, state.bee.y)) {
                drawBee(
                    center = Offset(state.bee.x, state.bee.y),
                    radius = state.bee.radius,
                    wingFrame = wingFrame
                )
            }
        }
    }
}

private fun DrawScope.drawPipePair(pipe: com.msa.compose_kmm.domain.PipePair) {
    val left = pipe.x - pipe.width / 2f
    val capHeight = 23f
    val capOverhang = 7f
    val playableBottom = GameConfig.WORLD_HEIGHT - GameConfig.GROUND_HEIGHT

    drawPipeBody(
        left = left,
        top = 0f,
        width = pipe.width,
        height = (pipe.topHeight - capHeight).coerceAtLeast(0f)
    )
    drawPipeCap(
        left = left - capOverhang,
        top = (pipe.topHeight - capHeight).coerceAtLeast(0f),
        width = pipe.width + capOverhang * 2f,
        height = capHeight
    )

    drawPipeCap(
        left = left - capOverhang,
        top = pipe.gapBottom,
        width = pipe.width + capOverhang * 2f,
        height = capHeight
    )
    drawPipeBody(
        left = left,
        top = pipe.gapBottom + capHeight,
        width = pipe.width,
        height = (playableBottom - pipe.gapBottom - capHeight).coerceAtLeast(0f)
    )
}

private fun DrawScope.drawPipeBody(left: Float, top: Float, width: Float, height: Float) {
    if (height <= 0f) return

    drawRoundRect(
        color = GameSoftShadow.copy(alpha = 0.34f),
        topLeft = Offset(left + 5f, top + 6f),
        size = Size(width + 1f, height),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        color = GamePipeRim,
        topLeft = Offset(left - 1.5f, top),
        size = Size(width + 3f, height),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(GamePipeDark, GamePipeMid, GamePipeLight, GamePipeMid),
            startX = left,
            endX = left + width
        ),
        topLeft = Offset(left + 1.5f, top + 1.5f),
        size = Size(width - 3f, (height - 3f).coerceAtLeast(0f)),
        cornerRadius = CornerRadius(7.5f, 7.5f)
    )
    drawRoundRect(
        color = GamePipeHighlight.copy(alpha = 0.76f),
        topLeft = Offset(left + width * 0.17f, top + 5f),
        size = Size(width * 0.12f, (height - 10f).coerceAtLeast(0f)),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.13f),
        topLeft = Offset(left + width * 0.34f, top + 5f),
        size = Size(width * 0.05f, (height - 10f).coerceAtLeast(0f)),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

private fun DrawScope.drawPipeCap(left: Float, top: Float, width: Float, height: Float) {
    drawRoundRect(
        color = GameSoftShadow.copy(alpha = 0.4f),
        topLeft = Offset(left + 4f, top + 5f),
        size = Size(width + 1f, height),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = GamePipeRim,
        topLeft = Offset(left - 1.5f, top - 1.5f),
        size = Size(width + 3f, height + 3f),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(GamePipeDark, GamePipeMid, GamePipeLight, GamePipeMid),
            startX = left,
            endX = left + width
        ),
        topLeft = Offset(left + 1.5f, top + 1.5f),
        size = Size(width - 3f, height - 3f),
        cornerRadius = CornerRadius(7f, 7f)
    )
    drawRoundRect(
        color = GamePipeHighlight.copy(alpha = 0.82f),
        topLeft = Offset(left + width * 0.14f, top + 4f),
        size = Size(width * 0.12f, height - 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(
        color = GamePipeRim.copy(alpha = 0.45f),
        radius = 2.4f,
        center = Offset(left + width - 11f, top + height / 2f)
    )
}

private fun DrawScope.drawBeeMotionTrail(center: Offset, radius: Float, velocity: Float) {
    val intensity = (kotlin.math.abs(velocity) / GameConfig.MAX_VERTICAL_VELOCITY).coerceIn(0.15f, 1f)
    repeat(3) { index ->
        val distance = radius * (0.75f + index * 0.46f)
        drawRoundRect(
            color = GameHoneyYellow.copy(alpha = (0.18f - index * 0.045f) * intensity),
            topLeft = Offset(
                center.x - radius * 1.35f - distance,
                center.y - radius * 0.09f + index * radius * 0.13f
            ),
            size = Size(radius * (0.62f - index * 0.1f), radius * 0.14f),
            cornerRadius = CornerRadius(radius * 0.08f)
        )
    }
}

private fun DrawScope.drawBee(center: Offset, radius: Float, wingFrame: Int) {
    val wingLift = when (wingFrame % 4) {
        0 -> -radius * 0.25f
        1 -> -radius * 0.52f
        2 -> -radius * 0.16f
        else -> -radius * 0.4f
    }

    drawOval(
        color = GameSoftShadow.copy(alpha = 0.25f),
        topLeft = Offset(center.x - radius * 0.96f, center.y - radius * 0.58f + 4f),
        size = Size(radius * 2.08f, radius * 1.48f)
    )

    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(GameBeeWing, GameBeeWing.copy(alpha = 0.38f)),
            start = Offset(center.x - radius, center.y - radius * 1.5f),
            end = center
        ),
        topLeft = Offset(center.x - radius * 0.92f, center.y - radius * 1.17f + wingLift),
        size = Size(radius * 1.23f, radius * 1.03f)
    )
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(GameBeeWing, GameBeeWing.copy(alpha = 0.38f)),
            start = Offset(center.x, center.y - radius * 1.5f),
            end = Offset(center.x + radius, center.y)
        ),
        topLeft = Offset(center.x - radius * 0.02f, center.y - radius * 1.21f + wingLift),
        size = Size(radius * 1.23f, radius * 1.03f)
    )

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(GameBeeYellowLight, GameBeeYellow, Color(0xFFF1A90D)),
            startX = center.x - radius,
            endX = center.x + radius
        ),
        topLeft = Offset(center.x - radius, center.y - radius * 0.72f),
        size = Size(radius * 2f, radius * 1.44f)
    )

    val stripeWidth = radius * 0.26f
    repeat(3) { index ->
        drawRoundRect(
            color = GameBeeBrown,
            topLeft = Offset(
                center.x - radius * 0.46f + index * radius * 0.42f,
                center.y - radius * 0.66f
            ),
            size = Size(stripeWidth, radius * 1.32f),
            cornerRadius = CornerRadius(radius * 0.12f)
        )
    }

    drawOval(
        color = Color.White.copy(alpha = 0.24f),
        topLeft = Offset(center.x - radius * 0.73f, center.y - radius * 0.56f),
        size = Size(radius * 0.66f, radius * 0.18f)
    )

    val headCenter = Offset(center.x + radius * 0.76f, center.y - radius * 0.03f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GameBeeBrown, GameBeeBrownDark),
            center = Offset(headCenter.x - radius * 0.14f, headCenter.y - radius * 0.16f),
            radius = radius * 0.62f
        ),
        radius = radius * 0.54f,
        center = headCenter
    )

    drawLine(
        color = GameBeeBrownDark,
        start = Offset(headCenter.x - radius * 0.08f, headCenter.y - radius * 0.47f),
        end = Offset(headCenter.x - radius * 0.22f, headCenter.y - radius * 0.82f),
        strokeWidth = radius * 0.075f
    )
    drawLine(
        color = GameBeeBrownDark,
        start = Offset(headCenter.x + radius * 0.14f, headCenter.y - radius * 0.46f),
        end = Offset(headCenter.x + radius * 0.27f, headCenter.y - radius * 0.8f),
        strokeWidth = radius * 0.075f
    )
    drawCircle(GameHoneyYellow, radius * 0.075f, Offset(headCenter.x - radius * 0.23f, headCenter.y - radius * 0.84f))
    drawCircle(GameHoneyYellow, radius * 0.075f, Offset(headCenter.x + radius * 0.28f, headCenter.y - radius * 0.82f))

    drawCircle(
        color = Color.White,
        radius = radius * 0.17f,
        center = Offset(headCenter.x + radius * 0.14f, headCenter.y - radius * 0.16f)
    )
    drawCircle(
        color = GameBeeBrownDark,
        radius = radius * 0.075f,
        center = Offset(headCenter.x + radius * 0.19f, headCenter.y - radius * 0.16f)
    )
    drawCircle(
        color = GameBeeCheek.copy(alpha = 0.86f),
        radius = radius * 0.09f,
        center = Offset(headCenter.x + radius * 0.34f, headCenter.y + radius * 0.1f)
    )
    drawArc(
        color = Color.White,
        startAngle = 18f,
        sweepAngle = 135f,
        useCenter = false,
        topLeft = Offset(headCenter.x + radius * 0.02f, headCenter.y - radius * 0.02f),
        size = Size(radius * 0.4f, radius * 0.32f),
        style = Stroke(width = radius * 0.065f)
    )

    drawLine(
        color = GameBeeBrownDark,
        start = Offset(center.x - radius * 0.18f, center.y + radius * 0.66f),
        end = Offset(center.x - radius * 0.28f, center.y + radius * 0.92f),
        strokeWidth = radius * 0.07f
    )
    drawLine(
        color = GameBeeBrownDark,
        start = Offset(center.x + radius * 0.27f, center.y + radius * 0.64f),
        end = Offset(center.x + radius * 0.18f, center.y + radius * 0.91f),
        strokeWidth = radius * 0.07f
    )

    val stinger = Path().apply {
        moveTo(center.x - radius * 1.01f, center.y - radius * 0.14f)
        lineTo(center.x - radius * 1.34f, center.y)
        lineTo(center.x - radius * 1.01f, center.y + radius * 0.14f)
        close()
    }
    drawPath(stinger, GameBeeBrownDark)
}
