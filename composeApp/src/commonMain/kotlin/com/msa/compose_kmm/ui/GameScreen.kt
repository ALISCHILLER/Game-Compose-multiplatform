package com.msa.compose_kmm.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.background
import compose_kmm.composeapp.generated.resources.moving_background
import org.jetbrains.compose.resources.painterResource

/**
 * پس‌زمینه بازی.
 *
 * شامل دو لایه است:
 * - تصویر اصلی پس‌زمینه
 * - زمین متحرک پایین صفحه
 */
@Composable
fun GameBackground(
    isRunning: Boolean
) {
    val movingOffsetX = remember {
        Animatable(0f)
    }

    var backgroundWidth by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        isRunning,
        backgroundWidth
    ) {
        if (!isRunning || backgroundWidth <= 0) {
            movingOffsetX.stop()
            return@LaunchedEffect
        }

        movingOffsetX.snapTo(0f)

        movingOffsetX.animateTo(
            targetValue = -backgroundWidth.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 5200,
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
            contentDescription = "پس‌زمینه بازی",
            contentScale = ContentScale.Crop
        )

        Image(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    backgroundWidth = it.width
                }
                .offset {
                    IntOffset(
                        x = movingOffsetX.value.toInt(),
                        y = 0
                    )
                },
            painter = painterResource(Res.drawable.moving_background),
            contentDescription = "زمین متحرک بازی",
            contentScale = ContentScale.FillBounds
        )

        Image(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = movingOffsetX.value.toInt() + backgroundWidth,
                        y = 0
                    )
                },
            painter = painterResource(Res.drawable.moving_background),
            contentDescription = "تکرار زمین متحرک بازی",
            contentScale = ContentScale.FillBounds
        )
    }
}