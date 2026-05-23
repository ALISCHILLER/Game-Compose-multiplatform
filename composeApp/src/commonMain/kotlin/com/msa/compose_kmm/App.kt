package com.msa.compose_kmm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import com.msa.compose_kmm.domain.AudioPlayer
import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameStatus
import com.msa.compose_kmm.domain.sprite.SpriteAnimationSpec
import com.msa.compose_kmm.domain.sprite.SpriteSheet
import com.msa.compose_kmm.domain.sprite.SpriteSpec
import com.msa.compose_kmm.domain.sprite.rememberSpriteState
import com.msa.compose_kmm.ui.GameBackground
import com.msa.compose_kmm.ui.GameCanvas
import com.msa.compose_kmm.ui.GameHud
import com.msa.compose_kmm.ui.GameOverOverlay
import com.msa.compose_kmm.ui.StartOverlay
import com.msa.compose_kmm.ui.ZarBeeGameTheme
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.bee_sprite
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val BEE_FRAME_SIZE = 80
private const val BEE_TOTAL_FRAMES = 9
private const val BEE_FRAMES_PER_ROW = 3
private const val BEE_FRAME_DURATION_MILLIS = 70L

/**
 * ورودی اصلی UI مشترک برنامه.
 */
@Composable
fun App() {
    ZarBeeGameTheme {
        GameRoot(audioPlayer = koinInject())
    }
}

/**
 * ریشه صفحه بازی.
 *
 * اینجا فقط هماهنگی بین UI، Game Loop، Sprite Animation و Audio انجام می‌شود.
 * منطق اصلی بازی داخل Game.kt باقی می‌ماند.
 */
@Composable
private fun GameRoot(
    audioPlayer: AudioPlayer
) {
    val game = remember { Game() }

    val animationSpec = remember {
        SpriteAnimationSpec(
            totalFrames = BEE_TOTAL_FRAMES,
            framesPerRow = BEE_FRAMES_PER_ROW,
            frameDurationMillis = BEE_FRAME_DURATION_MILLIS,
            loop = true
        )
    }

    val spriteState = rememberSpriteState(
        totalFrames = animationSpec.totalFrames,
        initialFrame = 0
    )

    val spriteSpec = remember {
        SpriteSpec(
            default = SpriteSheet(
                image = Res.drawable.bee_sprite,
                frameWidthPx = BEE_FRAME_SIZE,
                frameHeightPx = BEE_FRAME_SIZE
            )
        )
    }

    DisposableEffect(audioPlayer) {
        onDispose {
            audioPlayer.release()
        }
    }

    LaunchedEffect(game) {
        var previousFrameTime = 0L

        while (true) {
            withFrameMillis { currentFrameTime ->
                if (previousFrameTime != 0L && game.status == GameStatus.Started) {
                    val deltaMillis = currentFrameTime - previousFrameTime
                    game.update(deltaMillis)
                }

                previousFrameTime = currentFrameTime
            }
        }
    }

    LaunchedEffect(game.status, audioPlayer) {
        when (game.status) {
            GameStatus.Idle -> audioPlayer.stopGameSound()

            GameStatus.Started -> audioPlayer.playGameSoundInLoop()

            GameStatus.Over -> {
                audioPlayer.stopGameSound()
                audioPlayer.playGameOverSound()
            }
        }
    }

    LaunchedEffect(game.status, animationSpec) {
        when (game.status) {
            GameStatus.Idle -> spriteState.stop()

            GameStatus.Started -> {
                spriteState.play()

                while (game.status == GameStatus.Started) {
                    delay(animationSpec.frameDurationMillis)
                    spriteState.nextFrame(loop = animationSpec.loop)
                }
            }

            GameStatus.Over -> spriteState.pause()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GameBackground(
            isRunning = game.status == GameStatus.Started,
            groundHeightPx = game.groundHeight
        )

        GameCanvas(
            game = game,
            spriteSpec = spriteSpec,
            animationSpec = animationSpec,
            currentFrame = spriteState.currentFrame,
            onScreenSizeChanged = { width, height ->
                game.updateBounds(width = width, height = height)
            },
            onJump = {
                handleJump(
                    game = game,
                    audioPlayer = audioPlayer
                )
            }
        )

        GameHud(
            score = game.score,
            bestScore = game.bestScore
        )

        if (game.status == GameStatus.Idle) {
            StartOverlay(
                onStartClick = {
                    handleJump(
                        game = game,
                        audioPlayer = audioPlayer
                    )
                }
            )
        }

        if (game.status == GameStatus.Over) {
            GameOverOverlay(
                score = game.score,
                bestScore = game.bestScore,
                onRestartClick = {
                    game.restart()
                    audioPlayer.playJumpSound()
                    game.jump()
                }
            )
        }
    }
}

/**
 * مدیریت یکپارچه tap/click کاربر.
 */
private fun handleJump(
    game: Game,
    audioPlayer: AudioPlayer
) {
    when (game.status) {
        GameStatus.Idle -> {
            game.start()
            audioPlayer.playJumpSound()
            game.jump()
        }

        GameStatus.Started -> {
            audioPlayer.playJumpSound()
            game.jump()
        }

        GameStatus.Over -> Unit
    }
}