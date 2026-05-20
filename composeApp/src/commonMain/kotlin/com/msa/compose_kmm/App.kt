package com.msa.compose_kmm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.bee_sprite

private const val BEE_FRAME_SIZE = 80
private const val BEE_TOTAL_FRAMES = 9
private const val BEE_FRAMES_PER_ROW = 3

/**
 * ورودی اصلی UI بازی.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        GameScreen()
    }
}

/**
 * صفحه اصلی بازی.
 */
@Composable
private fun GameScreen() {
    var screenWidth by remember { mutableIntStateOf(0) }
    var screenHeight by remember { mutableIntStateOf(0) }

    var game by remember {
        mutableStateOf(Game())
    }

    val animationSpec = remember {
        SpriteAnimationSpec(
            totalFrames = BEE_TOTAL_FRAMES,
            framesPerRow = BEE_FRAMES_PER_ROW,
            frameDurationMillis = 70L,
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

    LaunchedEffect(game.status) {
        while (game.status == GameStatus.Started) {
            withFrameMillis {
                game.updateGameProgress()
            }
        }

        if (game.status == GameStatus.Over) {
            spriteState.pause()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GameBackground()

        GameCanvas(
            game = game,
            spriteSpec = spriteSpec,
            animationSpec = animationSpec,
            currentFrame = spriteState.currentFrame,
            onScreenSizeChanged = { width, height ->
                if (screenWidth != width || screenHeight != height) {
                    screenWidth = width
                    screenHeight = height

                    game = Game(
                        screenWith = width,
                        screenHeight = height
                    )
                }
            },
            onJump = {
                if (game.status == GameStatus.Started) {
                    game.jump()
                }
            }
        )

        GameHud(
            score = 0,
            bestScore = 0
        )

        if (game.status == GameStatus.Idle) {
            StartOverlay(
                onStartClick = {
                    game.start()
                    spriteState.play()
                }
            )
        }

        if (game.status == GameStatus.Over) {
            GameOverOverlay(
                score = 0,
                onRestartClick = {
                    game = Game(
                        screenWith = screenWidth,
                        screenHeight = screenHeight
                    )

                    game.start()

                    spriteState.stop()
                    spriteState.play()
                }
            )
        }
    }
}