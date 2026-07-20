package com.msa.compose_kmm.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.score_gained
import compose_kmm.composeapp.generated.resources.score_increment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Short visual and semantic feedback shown whenever the player passes a pipe. */
@Composable
fun ScoreFeedback(
    score: Int,
    visible: Boolean,
    reduceMotion: Boolean
) {
    val one = localizedNumber(1)
    val visualLabel = stringResource(Res.string.score_increment, one)
    val spokenLabel = stringResource(Res.string.score_gained, one)
    val feedbackAlpha = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    var previousScore by remember { mutableIntStateOf(score) }
    val density = LocalDensity.current

    LaunchedEffect(score, visible, reduceMotion) {
        if (!visible) {
            previousScore = score
            feedbackAlpha.snapTo(0f)
            rise.snapTo(0f)
            return@LaunchedEffect
        }

        if (score > previousScore) {
            feedbackAlpha.snapTo(1f)
            rise.snapTo(0f)
            if (reduceMotion) {
                delay(420L)
                feedbackAlpha.snapTo(0f)
            } else {
                coroutineScope {
                    launch {
                        rise.animateTo(
                            targetValue = -34f,
                            animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing)
                        )
                    }
                    launch {
                        delay(300L)
                        feedbackAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 390)
                        )
                    }
                }
            }
        }
        previousScore = score
    }

    if (feedbackAlpha.value <= 0.001f) return

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val topSpace = maxHeight * 0.24f
        Box(modifier = Modifier.padding(top = topSpace)) {
            Surface(
                modifier = Modifier
                    .testTag(UiTestTags.SCORE_FEEDBACK)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = spokenLabel
                    }
                    .graphicsLayer {
                        alpha = feedbackAlpha.value
                        translationY = with(density) { rise.value.dp.toPx() }
                        scaleX = 0.92f + feedbackAlpha.value * 0.08f
                        scaleY = 0.92f + feedbackAlpha.value * 0.08f
                    },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                color = GameHoneyYellow,
                contentColor = GameBeeBrownDark,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, GameHoneyLight)
            ) {
                Text(
                    text = visualLabel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
