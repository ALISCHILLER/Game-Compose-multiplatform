package com.msa.compose_kmm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.GameStatus
import com.msa.compose_kmm.domain.SaveDurability
import com.msa.compose_kmm.platform.AccessibilityPreferences
import com.msa.compose_kmm.presentation.GameController
import com.msa.compose_kmm.presentation.GameInputResult
import com.msa.compose_kmm.presentation.SettingsController
import com.msa.compose_kmm.ui.GameBackground
import com.msa.compose_kmm.ui.GameCanvas
import com.msa.compose_kmm.ui.GameHud
import com.msa.compose_kmm.ui.GameOverOverlay
import com.msa.compose_kmm.ui.GameplayHintBanner
import com.msa.compose_kmm.ui.MsaBeeGameTheme
import com.msa.compose_kmm.ui.ScoreFeedback
import com.msa.compose_kmm.ui.SettingsOverlay
import com.msa.compose_kmm.ui.StartOverlay
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.tap_to_jump
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val BEE_FRAME_DURATION_MILLIS = 80L
private const val PERSISTENCE_INTERVAL_MILLIS = 1_000L
private const val GAMEPLAY_HINT_DURATION_MILLIS = 3_500L

/** ورودی اصلی UI مشترک برنامه. */
@Composable
fun App() {
    MsaBeeGameTheme {
        GameRoot(
            audioPlayer = koinInject(),
            controller = koinInject(),
            settingsController = koinInject(),
            accessibilityPreferences = koinInject()
        )
    }
}

/** هماهنگ‌کننده‌ی Lifecycle، State holder، تنظیمات، صدا و UI. */
@Composable
private fun GameRoot(
    audioPlayer: AudioPlayer,
    controller: GameController,
    settingsController: SettingsController,
    accessibilityPreferences: AccessibilityPreferences
) {
    val gameState by controller.state.collectAsState()
    val settings by settingsController.state.collectAsState()
    var isLifecycleResumed by remember { mutableStateOf(false) }
    var systemReducedMotion by remember(accessibilityPreferences) {
        mutableStateOf(accessibilityPreferences.prefersReducedMotion())
    }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var wingFrame by remember { mutableIntStateOf(0) }
    var lastAudioStatus by remember { mutableStateOf(gameState.status) }
    var lastAudioScore by remember { mutableIntStateOf(gameState.score) }
    var gameplayHintVisible by remember { mutableStateOf(false) }

    val reduceMotion = systemReducedMotion || settings.reduceMotion
    val musicEnabled = settings.effectiveMusicVolume > 0f
    val effectsEnabled = settings.effectiveEffectsVolume > 0f
    val canRunGame = isLifecycleResumed && !isSettingsOpen
    val gameplayHint = stringResource(Res.string.tap_to_jump)

    DisposableEffect(audioPlayer, controller) {
        onDispose {
            controller.persist(SaveDurability.Immediate)
            audioPlayer.release()
        }
    }

    LifecycleResumeEffect(audioPlayer, controller, accessibilityPreferences) {
        systemReducedMotion = accessibilityPreferences.prefersReducedMotion()
        isLifecycleResumed = true
        onPauseOrDispose {
            isLifecycleResumed = false
            controller.persist(SaveDurability.Immediate)
            audioPlayer.stopGameSound()
            audioPlayer.stopGameOverSound()
        }
    }

    LaunchedEffect(settings.musicVolume, settings.effectsVolume, settings.soundEnabled, settings.musicEnabled, settings.effectsEnabled) {
        audioPlayer.setMusicVolume(settings.effectiveMusicVolume)
        audioPlayer.setEffectsVolume(settings.effectiveEffectsVolume)
    }

    LaunchedEffect(controller, gameState.status, canRunGame) {
        if (!canRunGame || gameState.status != GameStatus.Started) return@LaunchedEffect

        var previousFrameTimeNanos = 0L
        while (true) {
            withFrameNanos { currentFrameTimeNanos ->
                if (previousFrameTimeNanos != 0L) {
                    controller.updateNanos(currentFrameTimeNanos - previousFrameTimeNanos)
                }
                previousFrameTimeNanos = currentFrameTimeNanos
            }
        }
    }

    LaunchedEffect(controller, gameState.status, canRunGame) {
        if (!canRunGame || gameState.status != GameStatus.Started) return@LaunchedEffect
        while (true) {
            delay(PERSISTENCE_INTERVAL_MILLIS)
            controller.persist(SaveDurability.Deferred)
        }
    }

    LaunchedEffect(gameState.status, audioPlayer, canRunGame, musicEnabled) {
        val previousStatus = lastAudioStatus
        when (gameState.status) {
            GameStatus.Idle -> audioPlayer.stopGameSound()
            GameStatus.Started -> {
                if (canRunGame && musicEnabled) audioPlayer.playGameSoundInLoop()
                else audioPlayer.stopGameSound()
            }
            GameStatus.Over -> {
                audioPlayer.stopGameSound()
                if (canRunGame && effectsEnabled && previousStatus == GameStatus.Started) {
                    audioPlayer.playGameOverSound()
                }
            }
        }
        lastAudioStatus = gameState.status
    }

    LaunchedEffect(gameState.score, gameState.status, effectsEnabled, canRunGame) {
        if (
            gameState.status == GameStatus.Started &&
            gameState.score > lastAudioScore &&
            effectsEnabled &&
            canRunGame
        ) {
            audioPlayer.playScoreSound()
        }
        lastAudioScore = gameState.score
    }

    LaunchedEffect(gameState.status, canRunGame, reduceMotion) {
        if (gameState.status != GameStatus.Started || !canRunGame || reduceMotion) {
            wingFrame = 0
            return@LaunchedEffect
        }

        while (true) {
            delay(BEE_FRAME_DURATION_MILLIS)
            wingFrame = (wingFrame + 1) % 4
        }
    }

    LaunchedEffect(gameState.status, settings.showGameplayHints) {
        gameplayHintVisible = gameState.status == GameStatus.Started &&
            gameState.score == 0 &&
            settings.showGameplayHints
        if (gameplayHintVisible) {
            delay(GAMEPLAY_HINT_DURATION_MILLIS)
            gameplayHintVisible = false
        }
    }

    LaunchedEffect(gameState.score) {
        if (gameState.score > 0) gameplayHintVisible = false
    }

    fun startOrJump() {
        if (isSettingsOpen) return
        when (controller.startOrJump()) {
            GameInputResult.Started -> {
                audioPlayer.stopGameOverSound()
                if (musicEnabled) audioPlayer.playGameSoundInLoop()
                if (effectsEnabled) audioPlayer.playJumpSound()
            }

            GameInputResult.Jumped -> {
                // Browser autoplay may reject the initial lifecycle-triggered attempt.
                // Retrying inside the user gesture unlocks background audio safely.
                if (musicEnabled) audioPlayer.playGameSoundInLoop()
                if (effectsEnabled) audioPlayer.playJumpSound()
            }
            GameInputResult.Restarted,
            GameInputResult.Ignored -> Unit
        }
    }

    fun openSettings() {
        isSettingsOpen = true
        gameplayHintVisible = false
        audioPlayer.stopGameSound()
        audioPlayer.stopGameOverSound()
        controller.persist(SaveDurability.Immediate)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GameBackground(
            scrollOffset = gameState.pipePairs.firstOrNull()?.x ?: 0f,
            reduceMotion = reduceMotion
        )

        GameCanvas(
            state = gameState,
            wingFrame = wingFrame,
            enabled = gameState.status == GameStatus.Started && !isSettingsOpen,
            reduceMotion = reduceMotion,
            onJump = ::startOrJump
        )

        ScoreFeedback(
            score = gameState.score,
            visible = gameState.status == GameStatus.Started && !isSettingsOpen,
            reduceMotion = reduceMotion
        )

        GameplayHintBanner(
            text = gameplayHint,
            visible = gameplayHintVisible && !isSettingsOpen
        )

        GameHud(
            score = gameState.score,
            bestScore = gameState.bestScore,
            visible = gameState.status == GameStatus.Started && !isSettingsOpen,
            reduceMotion = reduceMotion,
            onSettingsClick = ::openSettings
        )

        if (gameState.status == GameStatus.Idle && !isSettingsOpen) {
            StartOverlay(
                bestScore = gameState.bestScore,
                onStartClick = ::startOrJump,
                onSettingsClick = ::openSettings
            )
        }

        if (gameState.status == GameStatus.Over && !isSettingsOpen) {
            GameOverOverlay(
                score = gameState.score,
                bestScore = gameState.bestScore,
                isNewRecord = gameState.isNewRecord,
                onRestartClick = {
                    audioPlayer.stopGameOverSound()
                    controller.restart()
                    if (musicEnabled) audioPlayer.playGameSoundInLoop()
                    if (effectsEnabled) audioPlayer.playJumpSound()
                },
                onSettingsClick = ::openSettings
            )
        }

        if (isSettingsOpen) {
            SettingsOverlay(
                settings = settings,
                systemReducedMotion = systemReducedMotion,
                gameInProgress = gameState.status == GameStatus.Started,
                onSoundEnabledChange = { settingsController.setSoundEnabled(it) },
                onMusicEnabledChange = { settingsController.setMusicEnabled(it) },
                onEffectsEnabledChange = { settingsController.setEffectsEnabled(it) },
                onMusicVolumeChange = { settingsController.setMusicVolume(it) },
                onEffectsVolumeChange = { settingsController.setEffectsVolume(it) },
                onReduceMotionChange = { settingsController.setReduceMotion(it) },
                onShowHintsChange = { settingsController.setShowGameplayHints(it) },
                onRestoreDefaults = { settingsController.restoreDefaults() },
                onResetProgress = {
                    audioPlayer.stopGameSound()
                    audioPlayer.stopGameOverSound()
                    controller.resetProgress()
                    isSettingsOpen = false
                },
                onClose = { isSettingsOpen = false }
            )
        }
    }
}
