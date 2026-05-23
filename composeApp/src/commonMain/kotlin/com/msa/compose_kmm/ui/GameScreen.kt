package com.msa.compose_kmm.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.background
import compose_kmm.composeapp.generated.resources.moving_background
import org.jetbrains.compose.resources.painterResource

@Composable
fun GameBackground(
    isRunning: Boolean,
    groundHeightPx: Float
) {
    val movingOffsetX = remember { Animatable(0f) }
    var groundWidth by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val groundHeightDp = with(density) { groundHeightPx.toDp() }

    LaunchedEffect(isRunning, groundWidth) {
        if (!isRunning || groundWidth <= 0) {
            movingOffsetX.stop()
            return@LaunchedEffect
        }

        movingOffsetX.snapTo(0f)
        movingOffsetX.animateTo(
            targetValue = -groundWidth.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4600,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            GameSoftShadow.copy(alpha = 0.22f)
                        )
                    )
                )
        )

        GroundTile(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .height(groundHeightDp)
                .fillMaxWidth()
                .onSizeChanged { groundWidth = it.width }
                .offset { IntOffset(x = movingOffsetX.value.toInt(), y = 0) }
        )

        GroundTile(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .height(groundHeightDp)
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = movingOffsetX.value.toInt() + groundWidth,
                        y = 0
                    )
                }
        )
    }
}

@Composable
private fun GroundTile(modifier: Modifier) {
    Image(
        modifier = modifier,
        painter = painterResource(Res.drawable.moving_background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )
}