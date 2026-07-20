package com.msa.compose_kmm.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.best_score
import compose_kmm.composeapp.generated.resources.control_keyboard_detail
import compose_kmm.composeapp.generated.resources.control_keyboard_title
import compose_kmm.composeapp.generated.resources.control_touch_detail
import compose_kmm.composeapp.generated.resources.control_touch_title
import compose_kmm.composeapp.generated.resources.game_mode_badge
import compose_kmm.composeapp.generated.resources.game_subtitle
import compose_kmm.composeapp.generated.resources.game_title
import compose_kmm.composeapp.generated.resources.privacy_note
import compose_kmm.composeapp.generated.resources.start_game
import compose_kmm.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun StartOverlay(
    bestScore: Int,
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val title = stringResource(Res.string.game_title)
    val subtitle = stringResource(Res.string.game_subtitle)
    val modeBadge = stringResource(Res.string.game_mode_badge)
    val startLabel = stringResource(Res.string.start_game)
    val settingsLabel = stringResource(Res.string.settings)
    val bestLabel = stringResource(Res.string.best_score)
    val privacyNote = stringResource(Res.string.privacy_note)
    val touchTitle = stringResource(Res.string.control_touch_title)
    val touchDetail = stringResource(Res.string.control_touch_detail)
    val keyboardTitle = stringResource(Res.string.control_keyboard_title)
    val keyboardDetail = stringResource(Res.string.control_keyboard_detail)
    val bestValue = localizedNumber(bestScore)
    val focusRequester = remember { FocusRequester() }
    val fontScale = LocalDensity.current.fontScale

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiTestTags.START_OVERLAY)
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
                        StartHeroBlock(
                            modifier = Modifier.weight(1.08f),
                            title = title,
                            subtitle = subtitle,
                            modeBadge = modeBadge,
                            mascotSizeDp = spec.mascotSizeDp,
                            compact = true
                        )
                        StartActionBlock(
                            modifier = Modifier
                                .weight(0.92f)
                                .widthIn(max = spec.actionMaxWidthDp.dp),
                            startLabel = startLabel,
                            settingsLabel = settingsLabel,
                            bestLabel = bestLabel,
                            bestValue = bestValue,
                            bestScore = bestScore,
                            privacyNote = privacyNote,
                            touchTitle = touchTitle,
                            touchDetail = touchDetail,
                            keyboardTitle = keyboardTitle,
                            keyboardDetail = keyboardDetail,
                            stackHints = false,
                            focusRequester = focusRequester,
                            compact = true,
                            onStartClick = onStartClick,
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
                        StartHeroBlock(
                            modifier = Modifier.widthIn(max = spec.titleMaxWidthDp.dp),
                            title = title,
                            subtitle = subtitle,
                            modeBadge = modeBadge,
                            mascotSizeDp = spec.mascotSizeDp,
                            compact = spec.compactHeight
                        )
                        StartActionBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = spec.actionMaxWidthDp.dp),
                            startLabel = startLabel,
                            settingsLabel = settingsLabel,
                            bestLabel = bestLabel,
                            bestValue = bestValue,
                            bestScore = bestScore,
                            privacyNote = privacyNote,
                            touchTitle = touchTitle,
                            touchDetail = touchDetail,
                            keyboardTitle = keyboardTitle,
                            keyboardDetail = keyboardDetail,
                            stackHints = spec.stackControlHints,
                            focusRequester = focusRequester,
                            compact = spec.compactHeight,
                            onStartClick = onStartClick,
                            onSettingsClick = onSettingsClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartHeroBlock(
    modifier: Modifier,
    title: String,
    subtitle: String,
    modeBadge: String,
    mascotSizeDp: Float,
    compact: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameBadge(text = modeBadge)
        BeeEmblem(
            modifier = Modifier.testTag(UiTestTags.START_MASCOT),
            size = mascotSizeDp.dp,
            mood = BeeMood.Happy,
            tiltDegrees = -4f
        )
        Text(
            text = title,
            color = GameTextWhite,
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.displaySmall
            },
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
    }
}

@Composable
private fun StartActionBlock(
    modifier: Modifier,
    startLabel: String,
    settingsLabel: String,
    bestLabel: String,
    bestValue: String,
    bestScore: Int,
    privacyNote: String,
    touchTitle: String,
    touchDetail: String,
    keyboardTitle: String,
    keyboardDetail: String,
    stackHints: Boolean,
    focusRequester: FocusRequester,
    compact: Boolean,
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bestScore > 0) {
            MetricPill(
                label = bestLabel,
                value = bestValue,
                modifier = Modifier.testTag(UiTestTags.START_BEST_SCORE)
            )
        }

        GamePrimaryButton(
            label = startLabel,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag(UiTestTags.START_BUTTON),
            compact = compact,
            onClick = onStartClick
        )

        GameSecondaryButton(
            label = settingsLabel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(UiTestTags.SETTINGS_BUTTON),
            compact = compact,
            onClick = onSettingsClick
        )

        StartControlHints(
            touchTitle = touchTitle,
            touchDetail = touchDetail,
            keyboardTitle = keyboardTitle,
            keyboardDetail = keyboardDetail,
            stacked = stackHints
        )

        Text(
            text = privacyNote,
            color = GameTextSubtle,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StartControlHints(
    touchTitle: String,
    touchDetail: String,
    keyboardTitle: String,
    keyboardDetail: String,
    stacked: Boolean
) {
    if (stacked) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            ControlHintPill(
                title = touchTitle,
                detail = touchDetail,
                modifier = Modifier.fillMaxWidth()
            )
            ControlHintPill(
                title = keyboardTitle,
                detail = keyboardDetail,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ControlHintPill(
                title = touchTitle,
                detail = touchDetail,
                modifier = Modifier.weight(1f)
            )
            ControlHintPill(
                title = keyboardTitle,
                detail = keyboardDetail,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
