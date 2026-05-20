package com.msa.compose_kmm.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.sprite.SpriteAnimationSpec
import com.msa.compose_kmm.domain.sprite.SpriteFlip
import com.msa.compose_kmm.domain.sprite.SpriteSpec
import com.msa.compose_kmm.domain.sprite.drawSpriteFrame
import org.jetbrains.compose.resources.imageResource

/**
 * Canvas اصلی بازی.
 *
 * تمام عناصر گرافیکی gameplay مثل زنبور، pipeها و obstacleها اینجا رسم می‌شوند.
 */
@Composable
fun GameCanvas(
    game: Game,
    spriteSpec: SpriteSpec,
    animationSpec: SpriteAnimationSpec,
    currentFrame: Int,
    onScreenSizeChanged: (width: Int, height: Int) -> Unit,
    onJump: () -> Unit
) {
    BoxWithConstraints {
        val selectedSheet = remember(
            spriteSpec,
            maxWidth.value
        ) {
            spriteSpec.sheetFor(maxWidth.value)
        }

        val spriteImage = imageResource(selectedSheet.image)

        val animatedAngle by animateFloatAsState(
            targetValue = when {
                game.beeVelocity > game.beeMaxVelocity / 1.1f -> 25f
                game.beeVelocity < 0f -> -12f
                else -> 0f
            },
            label = "BeeAngleAnimation"
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    onScreenSizeChanged(
                        it.size.width,
                        it.size.height
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onJump
                )
        ) {
            rotate(
                degrees = animatedAngle,
                pivot = Offset(
                    x = game.bee.x,
                    y = game.bee.y
                )
            ) {
                drawSpriteFrame(
                    image = spriteImage,
                    sheet = selectedSheet,
                    animationSpec = animationSpec,
                    frame = currentFrame,
                    offset = IntOffset(
                        x = (game.bee.x - selectedSheet.frameWidthPx / 2f).toInt(),
                        y = (game.bee.y - selectedSheet.frameHeightPx / 2f).toInt()
                    ),
                    spriteFlip = SpriteFlip.None
                )
            }
        }
    }
}