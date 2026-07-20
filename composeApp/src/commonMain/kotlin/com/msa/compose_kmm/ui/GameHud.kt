package com.msa.compose_kmm.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.best_score
import compose_kmm.composeapp.generated.resources.score
import compose_kmm.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource

/** Premium adaptive HUD with score feedback and stable accessibility semantics. */
@Composable
fun GameHud(
    score: Int,
    bestScore: Int,
    visible: Boolean,
    reduceMotion: Boolean,
    onSettingsClick: () -> Unit
) {
    if (!visible) return

    val scoreLabel = stringResource(Res.string.score)
    val bestScoreLabel = stringResource(Res.string.best_score)
    val settingsLabel = stringResource(Res.string.settings)
    val scoreValue = localizedNumber(score)
    val bestValue = localizedNumber(bestScore)
    val fontScale = LocalDensity.current.fontScale
    val scoreScale = remember { Animatable(1f) }
    var previousScore by remember { mutableIntStateOf(score) }

    LaunchedEffect(score, reduceMotion) {
        if (score > previousScore && !reduceMotion) {
            scoreScale.snapTo(1.18f)
            scoreScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            scoreScale.snapTo(1f)
        }
        previousScore = score
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
    ) {
        val spec = calculateResponsiveLayout(
            widthDp = maxWidth.value,
            heightDp = maxHeight.value,
            fontScale = fontScale
        )
        val outerHorizontal = if (spec.useCompactHud) 8.dp else 16.dp
        val outerVertical = if (spec.useCompactHud) 7.dp else 11.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = outerHorizontal, vertical = outerVertical),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = spec.hudMaxWidthDp.dp)
                    .fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    if (spec.useCompactHud) 19.dp else 24.dp
                ),
                color = GameHudSurface,
                contentColor = GameTextWhite,
                shadowElevation = if (spec.useCompactHud) 8.dp else 12.dp,
                border = BorderStroke(1.dp, GamePanelBorderSoft)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = if (spec.useCompactHud) 10.dp else 14.dp,
                        vertical = if (spec.useCompactHud) 7.dp else 9.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (spec.useCompactHud) 8.dp else 12.dp)
                ) {
                    BeeEmblem(
                        size = if (spec.useCompactHud) 34.dp else 42.dp,
                        mood = BeeMood.Happy,
                        tiltDegrees = -7f
                    )

                    HudMetric(
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag(UiTestTags.SCORE)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "$scoreLabel: $scoreValue"
                            },
                        label = scoreLabel,
                        value = scoreValue,
                        emphasized = true,
                        compact = spec.useCompactHud,
                        valueScale = scoreScale.value
                    )

                    HudDivider()

                    HudMetric(
                        modifier = Modifier
                            .weight(1f)
                            .testTag(UiTestTags.BEST_SCORE)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "$bestScoreLabel: $bestValue"
                            },
                        label = bestScoreLabel,
                        value = bestValue,
                        emphasized = false,
                        compact = spec.useCompactHud,
                        valueScale = 1f
                    )

                    HudDivider()

                    TextButton(
                        modifier = (if (spec.useCompactHud) {
                            Modifier.size(44.dp)
                        } else {
                            Modifier.widthIn(min = 88.dp).height(44.dp)
                        })
                            .testTag(UiTestTags.SETTINGS_BUTTON)
                            .semantics { contentDescription = settingsLabel },
                        onClick = onSettingsClick
                    ) {
                        Text(
                            text = if (spec.useCompactHud) "⚙" else settingsLabel,
                            color = GameHoneyLight,
                            style = if (spec.useCompactHud) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.labelLarge
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudMetric(
    modifier: Modifier,
    label: String,
    value: String,
    emphasized: Boolean,
    compact: Boolean,
    valueScale: Float
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = GameTextMuted,
            style = if (compact) {
                MaterialTheme.typography.labelSmall
            } else {
                MaterialTheme.typography.labelMedium
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = value,
            color = if (emphasized) GameHoneyYellow else GameTextWhite,
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else if (emphasized) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleLarge
            },
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = valueScale
                    scaleY = valueScale
                }
        )
    }
}

@Composable
private fun HudDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(GamePanelBorderSoft)
    )
}
