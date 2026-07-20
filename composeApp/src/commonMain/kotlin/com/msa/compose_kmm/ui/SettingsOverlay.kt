package com.msa.compose_kmm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.domain.GameSettings
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.accessibility_section
import compose_kmm.composeapp.generated.resources.cancel
import compose_kmm.composeapp.generated.resources.confirm_reset
import compose_kmm.composeapp.generated.resources.data_section
import compose_kmm.composeapp.generated.resources.done
import compose_kmm.composeapp.generated.resources.effects
import compose_kmm.composeapp.generated.resources.effects_detail
import compose_kmm.composeapp.generated.resources.effects_volume
import compose_kmm.composeapp.generated.resources.music
import compose_kmm.composeapp.generated.resources.music_detail
import compose_kmm.composeapp.generated.resources.music_volume
import compose_kmm.composeapp.generated.resources.reduced_motion
import compose_kmm.composeapp.generated.resources.reduced_motion_detail
import compose_kmm.composeapp.generated.resources.reset_progress
import compose_kmm.composeapp.generated.resources.reset_progress_confirm_message
import compose_kmm.composeapp.generated.resources.reset_progress_confirm_title
import compose_kmm.composeapp.generated.resources.reset_progress_detail
import compose_kmm.composeapp.generated.resources.restore_defaults
import compose_kmm.composeapp.generated.resources.settings_pause_note
import compose_kmm.composeapp.generated.resources.settings_subtitle
import compose_kmm.composeapp.generated.resources.settings_title
import compose_kmm.composeapp.generated.resources.show_hints
import compose_kmm.composeapp.generated.resources.show_hints_detail
import compose_kmm.composeapp.generated.resources.sound_enabled
import compose_kmm.composeapp.generated.resources.sound_enabled_detail
import compose_kmm.composeapp.generated.resources.sound_section
import compose_kmm.composeapp.generated.resources.system_reduced_motion_active
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsOverlay(
    settings: GameSettings,
    systemReducedMotion: Boolean,
    gameInProgress: Boolean,
    onSoundEnabledChange: (Boolean) -> Unit,
    onMusicEnabledChange: (Boolean) -> Unit,
    onEffectsEnabledChange: (Boolean) -> Unit,
    onMusicVolumeChange: (Int) -> Unit,
    onEffectsVolumeChange: (Int) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onShowHintsChange: (Boolean) -> Unit,
    onRestoreDefaults: () -> Unit,
    onResetProgress: () -> Unit,
    onClose: () -> Unit
) {
    val title = stringResource(Res.string.settings_title)
    val fontScale = LocalDensity.current.fontScale
    var confirmReset by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiTestTags.SETTINGS_OVERLAY)
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
            val availableWidth = maxWidth
            val spec = calculateResponsiveLayout(
                widthDp = availableWidth.value,
                heightDp = maxHeight.value,
                fontScale = fontScale
            )

            GamePanelSurface(
                modifier = Modifier
                    .padding(
                        horizontal = spec.outerHorizontalPaddingDp.dp,
                        vertical = spec.outerVerticalPaddingDp.dp
                    )
                    .widthIn(max = 820.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                cornerRadius = spec.panelCornerRadiusDp.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = spec.panelHorizontalPaddingDp.dp,
                        vertical = spec.panelVerticalPaddingDp.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(if (spec.compactHeight) 12.dp else 17.dp)
                ) {
                    SettingsHeader(
                        title = title,
                        subtitle = stringResource(Res.string.settings_subtitle),
                        pauseNote = stringResource(Res.string.settings_pause_note),
                        showPauseNote = gameInProgress
                    )

                    if (spec.useHorizontalOverlay && availableWidth >= 680.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            SoundSettingsSection(
                                modifier = Modifier.weight(1f),
                                settings = settings,
                                onSoundEnabledChange = onSoundEnabledChange,
                                onMusicEnabledChange = onMusicEnabledChange,
                                onEffectsEnabledChange = onEffectsEnabledChange,
                                onMusicVolumeChange = onMusicVolumeChange,
                                onEffectsVolumeChange = onEffectsVolumeChange
                            )
                            ComfortAndDataSection(
                                modifier = Modifier.weight(1f),
                                settings = settings,
                                systemReducedMotion = systemReducedMotion,
                                confirmReset = confirmReset,
                                onConfirmResetChange = { confirmReset = it },
                                onReduceMotionChange = onReduceMotionChange,
                                onShowHintsChange = onShowHintsChange,
                                onRestoreDefaults = onRestoreDefaults,
                                onResetProgress = onResetProgress
                            )
                        }
                    } else {
                        SoundSettingsSection(
                            modifier = Modifier.fillMaxWidth(),
                            settings = settings,
                            onSoundEnabledChange = onSoundEnabledChange,
                            onMusicEnabledChange = onMusicEnabledChange,
                            onEffectsEnabledChange = onEffectsEnabledChange,
                            onMusicVolumeChange = onMusicVolumeChange,
                            onEffectsVolumeChange = onEffectsVolumeChange
                        )
                        ComfortAndDataSection(
                            modifier = Modifier.fillMaxWidth(),
                            settings = settings,
                            systemReducedMotion = systemReducedMotion,
                            confirmReset = confirmReset,
                            onConfirmResetChange = { confirmReset = it },
                            onReduceMotionChange = onReduceMotionChange,
                            onShowHintsChange = onShowHintsChange,
                            onRestoreDefaults = onRestoreDefaults,
                            onResetProgress = onResetProgress
                        )
                    }

                    GamePrimaryButton(
                        label = stringResource(Res.string.done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 320.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag(UiTestTags.SETTINGS_DONE),
                        compact = spec.compactHeight,
                        onClick = onClose
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    subtitle: String,
    pauseNote: String,
    showPauseNote: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            color = GameTextWhite,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            color = GameTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (showPauseNote) {
            GameBadge(text = pauseNote)
        }
    }
}

@Composable
private fun SoundSettingsSection(
    modifier: Modifier,
    settings: GameSettings,
    onSoundEnabledChange: (Boolean) -> Unit,
    onMusicEnabledChange: (Boolean) -> Unit,
    onEffectsEnabledChange: (Boolean) -> Unit,
    onMusicVolumeChange: (Int) -> Unit,
    onEffectsVolumeChange: (Int) -> Unit
) {
    SettingsSectionCard(
        modifier = modifier,
        title = stringResource(Res.string.sound_section)
    ) {
        SettingsToggleRow(
            title = stringResource(Res.string.sound_enabled),
            detail = stringResource(Res.string.sound_enabled_detail),
            checked = settings.soundEnabled,
            onCheckedChange = onSoundEnabledChange,
            testTag = UiTestTags.SOUND_TOGGLE
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(Res.string.music),
            detail = stringResource(Res.string.music_detail),
            checked = settings.musicEnabled,
            enabled = settings.soundEnabled,
            onCheckedChange = onMusicEnabledChange
        )
        SettingsVolumeRow(
            title = stringResource(Res.string.music_volume),
            value = settings.musicVolume,
            enabled = settings.soundEnabled && settings.musicEnabled,
            onValueChange = onMusicVolumeChange
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(Res.string.effects),
            detail = stringResource(Res.string.effects_detail),
            checked = settings.effectsEnabled,
            enabled = settings.soundEnabled,
            onCheckedChange = onEffectsEnabledChange
        )
        SettingsVolumeRow(
            title = stringResource(Res.string.effects_volume),
            value = settings.effectsVolume,
            enabled = settings.soundEnabled && settings.effectsEnabled,
            onValueChange = onEffectsVolumeChange
        )
    }
}

@Composable
private fun ComfortAndDataSection(
    modifier: Modifier,
    settings: GameSettings,
    systemReducedMotion: Boolean,
    confirmReset: Boolean,
    onConfirmResetChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onShowHintsChange: (Boolean) -> Unit,
    onRestoreDefaults: () -> Unit,
    onResetProgress: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSectionCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.accessibility_section)
        ) {
            SettingsToggleRow(
                title = stringResource(Res.string.reduced_motion),
                detail = stringResource(Res.string.reduced_motion_detail),
                checked = settings.reduceMotion || systemReducedMotion,
                enabled = !systemReducedMotion,
                onCheckedChange = onReduceMotionChange,
                testTag = UiTestTags.REDUCE_MOTION_TOGGLE
            )
            if (systemReducedMotion) {
                Text(
                    text = stringResource(Res.string.system_reduced_motion_active),
                    color = GameHoneyLight,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SettingsDivider()
            SettingsToggleRow(
                title = stringResource(Res.string.show_hints),
                detail = stringResource(Res.string.show_hints_detail),
                checked = settings.showGameplayHints,
                onCheckedChange = onShowHintsChange
            )
        }

        SettingsSectionCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.data_section)
        ) {
            if (!confirmReset) {
                Text(
                    text = stringResource(Res.string.reset_progress_detail),
                    color = GameTextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(UiTestTags.RESET_PROGRESS_BUTTON),
                    onClick = { onConfirmResetChange(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameDanger,
                        contentColor = GameTextWhite
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.reset_progress),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                ResetConfirmation(
                    onCancel = { onConfirmResetChange(false) },
                    onConfirm = onResetProgress
                )
            }

            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onRestoreDefaults
            ) {
                Text(
                    text = stringResource(Res.string.restore_defaults),
                    color = GameHoneyLight,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    modifier: Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = GameCardDark,
        contentColor = GameTextWhite,
        border = BorderStroke(1.dp, GamePanelBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = title,
                color = GameHoneyYellow,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    detail: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = if (enabled) GameTextWhite else GameTextSubtle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                color = if (enabled) GameTextMuted else GameTextSubtle,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            modifier = if (testTag == null) Modifier else Modifier.testTag(testTag),
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsVolumeRow(
    title: String,
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (enabled) GameTextMuted else GameTextSubtle,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${localizedNumber(sliderValue.roundToInt())}%",
                color = if (enabled) GameHoneyYellow else GameTextSubtle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue.roundToInt()) },
            valueRange = 0f..100f,
            enabled = enabled
        )
    }
}

@Composable
private fun ResetConfirmation(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GameDanger.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, GameDanger)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.reset_progress_confirm_title),
                color = GameTextWhite,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(Res.string.reset_progress_confirm_message),
                color = GameTextMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancel
                ) {
                    Text(stringResource(Res.string.cancel), color = GameTextMuted)
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameDanger,
                        contentColor = GameTextWhite
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.confirm_reset),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GamePanelBorderSoft)
    )
}
