package com.msa.compose_kmm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import com.msa.compose_kmm.ui.GameHud
import com.msa.compose_kmm.ui.GameOverOverlay
import com.msa.compose_kmm.ui.MsaBeeGameTheme
import com.msa.compose_kmm.ui.SettingsOverlay
import com.msa.compose_kmm.ui.StartOverlay
import com.msa.compose_kmm.domain.GameSettings
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.ui.UiTestTags
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ComposeUiTest {
    @Test
    fun startOverlayExposesAWorkingPrimaryAction() = runComposeUiTest {
        var clicked = false
        setContent {
            MsaBeeGameTheme {
                StartOverlay(bestScore = 12, onStartClick = { clicked = true }, onSettingsClick = {})
            }
        }

        onNodeWithTag(UiTestTags.START_OVERLAY).assertIsDisplayed()
        onNodeWithTag(UiTestTags.START_BUTTON).assertIsDisplayed().performClick()
        runOnIdle { assertTrue(clicked) }
    }

    @Test
    fun startOverlayRemainsUsableAtLargeFontScaleInACompactWindow() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                MsaBeeGameTheme {
                    Box(Modifier.size(width = 260.dp, height = 360.dp)) {
                        StartOverlay(bestScore = 12, onStartClick = {}, onSettingsClick = {})
                    }
                }
            }
        }

        onNodeWithTag(UiTestTags.START_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun hudExposesScoreAndBestScoreSemantics() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                GameHud(score = 4, bestScore = 9, visible = true, reduceMotion = true, onSettingsClick = {})
            }
        }

        onNodeWithTag(UiTestTags.SCORE).assertIsDisplayed()
        onNodeWithTag(UiTestTags.BEST_SCORE).assertIsDisplayed()
    }

    @Test
    fun gameOverOverlayRemainsUsableAtLargeFontScaleInACompactWindow() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                MsaBeeGameTheme {
                    Box(Modifier.size(width = 260.dp, height = 380.dp)) {
                        GameOverOverlay(score = 23, bestScore = 41, onRestartClick = {}, onSettingsClick = {})
                    }
                }
            }
        }

        onNodeWithTag(UiTestTags.RESTART_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }


    @Test
    fun gameOverOverlayHighlightsANewRecord() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                GameOverOverlay(
                    score = 12,
                    bestScore = 12,
                    isNewRecord = true,
                    onRestartClick = {},
                    onSettingsClick = {}
                )
            }
        }

        onNodeWithTag(UiTestTags.NEW_RECORD).assertIsDisplayed()
    }


    @Test
    fun gameOverOverlayDoesNotCelebrateATiedRecord() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                GameOverOverlay(
                    score = 12,
                    bestScore = 12,
                    isNewRecord = false,
                    onRestartClick = {},
                    onSettingsClick = {}
                )
            }
        }

        onNodeWithTag(UiTestTags.NEW_RECORD).assertDoesNotExist()
    }

    @Test
    fun gameOverOverlayExposesRestartAction() = runComposeUiTest {
        var restarted = false
        setContent {
            MsaBeeGameTheme {
                GameOverOverlay(score = 3, bestScore = 8, onRestartClick = { restarted = true }, onSettingsClick = {})
            }
        }

        onNodeWithTag(UiTestTags.GAME_OVER_OVERLAY).assertIsDisplayed()
        onNodeWithTag(UiTestTags.RESTART_BUTTON).assertIsDisplayed().performClick()
        runOnIdle { assertTrue(restarted) }
    }
    @Test
    fun startOverlayWorksOnLowHeightPhoneLandscape() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                Box(Modifier.size(width = 800.dp, height = 320.dp)) {
                    StartOverlay(bestScore = 12, onStartClick = {}, onSettingsClick = {})
                }
            }
        }

        onNodeWithTag(UiTestTags.START_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun startOverlayWorksOnLowHeightLandscapeAtTwoHundredPercentFontScale() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                MsaBeeGameTheme {
                    Box(Modifier.size(width = 800.dp, height = 320.dp)) {
                        StartOverlay(bestScore = 12, onStartClick = {}, onSettingsClick = {})
                    }
                }
            }
        }

        onNodeWithTag(UiTestTags.START_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun gameOverOverlayWorksOnPortraitTablet() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                Box(Modifier.size(width = 800.dp, height = 1280.dp)) {
                    GameOverOverlay(score = 23, bestScore = 41, onRestartClick = {}, onSettingsClick = {})
                }
            }
        }

        onNodeWithTag(UiTestTags.RESTART_BUTTON).assertIsDisplayed()
    }

    @Test
    fun gameOverOverlayWorksOnLowHeightTabletLandscape() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                Box(Modifier.size(width = 1024.dp, height = 420.dp)) {
                    GameOverOverlay(score = 23, bestScore = 41, onRestartClick = {}, onSettingsClick = {})
                }
            }
        }

        onNodeWithTag(UiTestTags.RESTART_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun compactHudRemainsVisibleOnSmallPhoneAndLandscape() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                Box(Modifier.size(width = 320.dp, height = 568.dp)) {
                    GameHud(score = 4, bestScore = 9, visible = true, reduceMotion = true, onSettingsClick = {})
                }
            }
        }

        onNodeWithTag(UiTestTags.SCORE).assertIsDisplayed()
        onNodeWithTag(UiTestTags.BEST_SCORE).assertIsDisplayed()
    }

    @Test
    fun gameOverOverlayWorksOnLowHeightLandscapeAtTwoHundredPercentFontScale() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                MsaBeeGameTheme {
                    Box(Modifier.size(width = 800.dp, height = 320.dp)) {
                        GameOverOverlay(score = 23, bestScore = 41, onRestartClick = {}, onSettingsClick = {})
                    }
                }
            }
        }

        onNodeWithTag(UiTestTags.RESTART_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun compactHudRemainsVisibleOnLowHeightLandscape() = runComposeUiTest {
        setContent {
            MsaBeeGameTheme {
                Box(Modifier.size(width = 800.dp, height = 320.dp)) {
                    GameHud(score = 4, bestScore = 9, visible = true, reduceMotion = true, onSettingsClick = {})
                }
            }
        }

        onNodeWithTag(UiTestTags.SCORE).assertIsDisplayed()
        onNodeWithTag(UiTestTags.BEST_SCORE).assertIsDisplayed()
    }

    @Test
    fun settingsOverlayExposesSoundAndCloseControls() = runComposeUiTest {
        var soundEnabled = true
        var closed = false
        setContent {
            MsaBeeGameTheme {
                SettingsOverlay(
                    settings = GameSettings(soundEnabled = soundEnabled),
                    systemReducedMotion = false,
                    gameInProgress = true,
                    onSoundEnabledChange = { soundEnabled = it },
                    onMusicEnabledChange = {},
                    onEffectsEnabledChange = {},
                    onMusicVolumeChange = {},
                    onEffectsVolumeChange = {},
                    onReduceMotionChange = {},
                    onShowHintsChange = {},
                    onRestoreDefaults = {},
                    onResetProgress = {},
                    onClose = { closed = true }
                )
            }
        }

        onNodeWithTag(UiTestTags.SETTINGS_OVERLAY).assertIsDisplayed()
        onNodeWithTag(UiTestTags.SOUND_TOGGLE).assertIsDisplayed().performClick()
        onNodeWithTag(UiTestTags.SETTINGS_DONE).performScrollTo().assertIsDisplayed().performClick()
        runOnIdle {
            assertTrue(!soundEnabled)
            assertTrue(closed)
        }
    }

}
