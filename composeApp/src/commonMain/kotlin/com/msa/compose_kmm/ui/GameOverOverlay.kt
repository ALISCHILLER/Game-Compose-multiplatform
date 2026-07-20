package com.msa.compose_kmm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.best_score
import compose_kmm.composeapp.generated.resources.game_over
import compose_kmm.composeapp.generated.resources.game_over_subtitle
import compose_kmm.composeapp.generated.resources.new_record
import compose_kmm.composeapp.generated.resources.restart_game
import compose_kmm.composeapp.generated.resources.restart_hint
import compose_kmm.composeapp.generated.resources.score
import compose_kmm.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    isNewRecord: Boolean = false,
    onRestartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val title = stringResource(Res.string.game_over)
    val subtitle = stringResource(Res.string.game_over_subtitle)
    val scoreLabel = stringResource(Res.string.score)
    val bestScoreLabel = stringResource(Res.string.best_score)
    val restartLabel = stringResource(Res.string.restart_game)
    val settingsLabel = stringResource(Res.string.settings)
    val restartHint = stringResource(Res.string.restart_hint)
    val newRecordLabel = stringResource(Res.string.new_record)
    val scoreValue = localizedNumber(score)
    val bestScoreValue = localizedNumber(bestScore)
    val focusRequester = remember { FocusRequester() }
    val fontScale = LocalDensity.current.fontScale

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiTestTags.GAME_OVER_OVERLAY)
            .semantics {
                paneTitle = title
                isTraversalGroup = true
            }
    ) {
        OverlayBackdrop()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            val spec = calculateResponsiveLayout(
                widthDp = maxWidth.value,
                heightDp = maxHeight.value,
                fontScale = fontScale
            )

            GamePanelSurface(
                modifier = Modifier
                    .padding(
                        horizontal = spec.outerHorizontalPaddingDp.dp,
                        vertical = spec.outerVerticalPaddingDp.dp
                    )
                    .widthIn(max = spec.panelMaxWidthDp.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                cornerRadius = spec.panelCornerRadiusDp.dp
            ) {
                if (spec.useHorizontalOverlay) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = spec.panelHorizontalPaddingDp.dp,
                            vertical = spec.panelVerticalPaddingDp.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spec.contentSpacingDp.dp)
                    ) {
                        GameOverSummary(
                            modifier = Modifier.weight(1.2f),
                            title = title,
                            subtitle = subtitle,
                            scoreLabel = scoreLabel,
                            scoreValue = scoreValue,
                            bestScoreLabel = bestScoreLabel,
                            bestScoreValue = bestScoreValue,
                            newRecordLabel = newRecordLabel,
                            isNewRecord = isNewRecord,
                            mascotSizeDp = spec.mascotSizeDp,
                            compact = true,
                            stackCards = false
                        )
                        RestartAction(
                            modifier = Modifier
                                .weight(0.8f)
                                .widthIn(max = spec.actionMaxWidthDp.dp),
                            label = restartLabel,
                            settingsLabel = settingsLabel,
                            hint = restartHint,
                            focusRequester = focusRequester,
                            compact = true,
                            onRestartClick = onRestartClick,
                            onSettingsClick = onSettingsClick
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = spec.panelHorizontalPaddingDp.dp,
                            vertical = spec.panelVerticalPaddingDp.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(spec.contentSpacingDp.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameOverSummary(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = spec.titleMaxWidthDp.dp),
                            title = title,
                            subtitle = subtitle,
                            scoreLabel = scoreLabel,
                            scoreValue = scoreValue,
                            bestScoreLabel = bestScoreLabel,
                            bestScoreValue = bestScoreValue,
                            newRecordLabel = newRecordLabel,
                            isNewRecord = isNewRecord,
                            mascotSizeDp = spec.mascotSizeDp,
                            compact = spec.compactHeight,
                            stackCards = spec.stackScoreCards
                        )
                        RestartAction(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = spec.actionMaxWidthDp.dp),
                            label = restartLabel,
                            settingsLabel = settingsLabel,
                            hint = restartHint,
                            focusRequester = focusRequester,
                            compact = spec.compactHeight,
                            onRestartClick = onRestartClick,
                            onSettingsClick = onSettingsClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverSummary(
    modifier: Modifier,
    title: String,
    subtitle: String,
    scoreLabel: String,
    scoreValue: String,
    bestScoreLabel: String,
    bestScoreValue: String,
    newRecordLabel: String,
    isNewRecord: Boolean,
    mascotSizeDp: Float,
    compact: Boolean,
    stackCards: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BeeEmblem(
            size = mascotSizeDp.dp,
            mood = if (isNewRecord) BeeMood.Proud else BeeMood.Tired,
            tiltDegrees = if (isNewRecord) 4f else -6f
        )

        if (isNewRecord) {
            GameBadge(
                text = newRecordLabel,
                emphasized = true,
                modifier = Modifier
                    .testTag(UiTestTags.NEW_RECORD)
                    .semantics { contentDescription = newRecordLabel }
            )
        }

        Text(
            text = title,
            color = GameTextWhite,
            style = if (compact) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.displaySmall
            },
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            color = GameTextMuted,
            style = if (compact) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.titleMedium
            },
            textAlign = TextAlign.Center
        )

        ScoreCards(
            scoreLabel = scoreLabel,
            scoreValue = scoreValue,
            bestScoreLabel = bestScoreLabel,
            bestScoreValue = bestScoreValue,
            compact = compact,
            stackCards = stackCards,
            highlightBest = isNewRecord
        )
    }
}

@Composable
private fun ScoreCards(
    scoreLabel: String,
    scoreValue: String,
    bestScoreLabel: String,
    bestScoreValue: String,
    compact: Boolean,
    stackCards: Boolean,
    highlightBest: Boolean
) {
    if (stackCards) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ResultScoreCard(
                modifier = Modifier.fillMaxWidth(),
                label = scoreLabel,
                value = scoreValue,
                compact = compact,
                highlighted = false
            )
            ResultScoreCard(
                modifier = Modifier.fillMaxWidth(),
                label = bestScoreLabel,
                value = bestScoreValue,
                compact = compact,
                highlighted = highlightBest
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultScoreCard(
                modifier = Modifier.weight(1f),
                label = scoreLabel,
                value = scoreValue,
                compact = compact,
                highlighted = false
            )
            ResultScoreCard(
                modifier = Modifier.weight(1f),
                label = bestScoreLabel,
                value = bestScoreValue,
                compact = compact,
                highlighted = highlightBest
            )
        }
    }
}

@Composable
private fun RestartAction(
    modifier: Modifier,
    label: String,
    settingsLabel: String,
    hint: String,
    focusRequester: FocusRequester,
    compact: Boolean,
    onRestartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GamePrimaryButton(
            label = label,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag(UiTestTags.RESTART_BUTTON),
            compact = compact,
            onClick = onRestartClick
        )
        GameSecondaryButton(
            label = settingsLabel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(UiTestTags.SETTINGS_BUTTON),
            compact = compact,
            onClick = onSettingsClick
        )
        Text(
            text = hint,
            color = GameTextSubtle,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultScoreCard(
    modifier: Modifier,
    label: String,
    value: String,
    compact: Boolean,
    highlighted: Boolean
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(if (compact) 16.dp else 20.dp),
        color = if (highlighted) GameGoldDeep.copy(alpha = 0.44f) else GameCardDark,
        contentColor = GameTextWhite,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) GameHoneyYellow else GamePanelBorderSoft
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 9.dp else 14.dp,
                vertical = if (compact) 9.dp else 13.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (highlighted) GameHoneyLight else GameTextMuted,
                style = if (compact) {
                    MaterialTheme.typography.labelLarge
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                color = GameHoneyYellow,
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
