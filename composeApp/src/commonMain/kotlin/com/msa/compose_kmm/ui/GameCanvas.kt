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
import androidx.compose.ui.unit.IntSize
import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.sprite.SpriteAnimationSpec
import com.msa.compose_kmm.domain.sprite.SpriteFlip
import com.msa.compose_kmm.domain.sprite.SpriteSpec
import com.msa.compose_kmm.domain.sprite.drawSpriteFrame
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.pipe
import compose_kmm.composeapp.generated.resources.pipe_cap
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/**
 * Canvas اصلی بازی.
 *
 * اینجا تمام عناصر گیم‌پلی رسم می‌شوند:
 * - لوله‌ها
 * - زنبور
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

        val beeImage = imageResource(selectedSheet.image)
        val pipeImage = imageResource(Res.drawable.pipe)
        val pipeCapImage = imageResource(Res.drawable.pipe_cap)

        val animatedAngle by animateFloatAsState(
            targetValue = when {
                game.beeVelocity > game.beeMaxVelocity * 0.75f -> 26f
                game.beeVelocity < 0f -> -15f
                else -> 4f
            },
            label = "BeeRotationAnimation"
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
            /**
             * رسم لوله‌ها.
             */
            game.pipePairs.forEach { pipe ->
                val pipeWidth = pipe.width.roundToInt()
                val pipeLeft = (pipe.x - pipe.width / 2f).roundToInt()

                /**
                 * نسبت cap به body با توجه به asset فعلی pipe_cap.
                 */
                val capHeight = (pipe.width * 0.54f)
                    .roundToInt()
                    .coerceAtLeast(18)

                val topBodyHeight = (pipe.topHeight - capHeight)
                    .roundToInt()
                    .coerceAtLeast(0)

                val bottomCapY = pipe.gapBottom
                    .roundToInt()
                    .coerceAtLeast(0)

                val bottomBodyY = bottomCapY + capHeight

                val bottomBodyHeight = (
                        size.height - game.groundHeight - bottomBodyY
                        )
                    .roundToInt()
                    .coerceAtLeast(0)

                /**
                 * بدنه لوله بالایی.
                 */
                if (topBodyHeight > 0) {
                    drawImage(
                        image = pipeImage,
                        dstOffset = IntOffset(
                            x = pipeLeft,
                            y = 0
                        ),
                        dstSize = IntSize(
                            width = pipeWidth,
                            height = topBodyHeight
                        )
                    )
                }

                /**
                 * کلاهک لوله بالایی.
                 */
                drawImage(
                    image = pipeCapImage,
                    dstOffset = IntOffset(
                        x = pipeLeft,
                        y = topBodyHeight
                    ),
                    dstSize = IntSize(
                        width = pipeWidth,
                        height = capHeight
                    )
                )

                /**
                 * کلاهک لوله پایینی.
                 */
                drawImage(
                    image = pipeCapImage,
                    dstOffset = IntOffset(
                        x = pipeLeft,
                        y = bottomCapY
                    ),
                    dstSize = IntSize(
                        width = pipeWidth,
                        height = capHeight
                    )
                )

                /**
                 * بدنه لوله پایینی.
                 */
                if (bottomBodyHeight > 0) {
                    drawImage(
                        image = pipeImage,
                        dstOffset = IntOffset(
                            x = pipeLeft,
                            y = bottomBodyY
                        ),
                        dstSize = IntSize(
                            width = pipeWidth,
                            height = bottomBodyHeight
                        )
                    )
                }
            }

            /**
             * رسم زنبور.
             */
            rotate(
                degrees = animatedAngle,
                pivot = Offset(
                    x = game.bee.x,
                    y = game.bee.y
                )
            ) {
                drawSpriteFrame(
                    image = beeImage,
                    sheet = selectedSheet,
                    animationSpec = animationSpec,
                    frame = currentFrame,
                    offset = IntOffset(
                        x = (game.bee.x - selectedSheet.frameWidthPx / 2f).roundToInt(),
                        y = (game.bee.y - selectedSheet.frameHeightPx / 2f).roundToInt()
                    ),
                    dstSize = IntSize(
                        width = selectedSheet.frameWidthPx,
                        height = selectedSheet.frameHeightPx
                    ),
                    spriteFlip = SpriteFlip.None
                )
            }
        }
    }
}