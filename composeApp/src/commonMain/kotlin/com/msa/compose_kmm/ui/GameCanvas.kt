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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
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
 * این Composable فقط وظیفه رسم دارد:
 * - رسم لوله‌ها
 * - رسم سایه زنبور
 * - رسم sprite frame زنبور
 * - دریافت click برای پرش
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
        val selectedSheet = remember(spriteSpec, maxWidth.value) {
            spriteSpec.sheetFor(maxWidth.value)
        }

        val beeImage = imageResource(selectedSheet.image)
        val pipeImage = imageResource(Res.drawable.pipe)
        val pipeCapImage = imageResource(Res.drawable.pipe_cap)

        val animatedAngle by animateFloatAsState(
            targetValue = when {
                game.beeVelocity > game.beeMaxVelocity * 0.62f -> 24f
                game.beeVelocity < 0f -> -18f
                else -> 5f
            },
            label = "BeeRotationAnimation"
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    onScreenSizeChanged(it.size.width, it.size.height)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onJump
                )
        ) {
            game.pipePairs.forEach { pipe ->
                val pipeWidth = pipe.width.roundToInt()
                val pipeLeft = (pipe.x - pipe.width / 2f).roundToInt()

                val capHeight = (pipe.width * 0.62f)
                    .roundToInt()
                    .coerceAtLeast(28)

                val topBodyHeight = (pipe.topHeight - capHeight)
                    .roundToInt()
                    .coerceAtLeast(0)

                val bottomCapY = pipe.gapBottom
                    .roundToInt()
                    .coerceAtLeast(0)

                val bottomBodyY = bottomCapY + capHeight

                val bottomBodyHeight = (size.height - game.groundHeight - bottomBodyY)
                    .roundToInt()
                    .coerceAtLeast(0)

                val bodySrcOffset = IntOffset(
                    x = 0,
                    y = (pipeImage.height * 0.34f).roundToInt()
                )

                val bodySrcSize = IntSize(
                    width = pipeImage.width,
                    height = (pipeImage.height * 0.38f)
                        .roundToInt()
                        .coerceAtLeast(1)
                )

                // سایه لوله بالا
                drawRect(
                    color = GamePipeShadow,
                    topLeft = Offset(pipeLeft + pipeWidth * 0.08f, 0f),
                    size = Size(
                        width = pipeWidth.toFloat(),
                        height = topBodyHeight + capHeight.toFloat()
                    )
                )

                // بدنه لوله بالا
                if (topBodyHeight > 0) {
                    drawImage(
                        image = pipeImage,
                        srcOffset = bodySrcOffset,
                        srcSize = bodySrcSize,
                        dstOffset = IntOffset(x = pipeLeft, y = 0),
                        dstSize = IntSize(
                            width = pipeWidth,
                            height = topBodyHeight
                        )
                    )
                }

                // کلاهک لوله بالا؛ وارونه رسم می‌شود.
                withTransform({
                    scale(
                        scaleX = 1f,
                        scaleY = -1f,
                        pivot = Offset(
                            x = pipe.x,
                            y = topBodyHeight + capHeight / 2f
                        )
                    )
                }) {
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
                }

                // سایه لوله پایین
                drawRect(
                    color = GamePipeShadow,
                    topLeft = Offset(
                        x = pipeLeft + pipeWidth * 0.08f,
                        y = bottomCapY.toFloat()
                    ),
                    size = Size(
                        width = pipeWidth.toFloat(),
                        height = (capHeight + bottomBodyHeight).toFloat()
                    )
                )

                // کلاهک لوله پایین
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

                // بدنه لوله پایین
                if (bottomBodyHeight > 0) {
                    drawImage(
                        image = pipeImage,
                        srcOffset = bodySrcOffset,
                        srcSize = bodySrcSize,
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

            val floorY = size.height - game.groundHeight
            val shadowWidth = game.bee.radius * 2.15f
            val distanceToGround = (floorY - game.bee.y).coerceAtLeast(0f)

            val shadowAlpha = (1f - distanceToGround / (size.height * 0.58f))
                .coerceIn(0.12f, 0.42f)

            // سایه نرم زیر زنبور
            drawOval(
                color = GameSoftShadow.copy(alpha = shadowAlpha),
                topLeft = Offset(
                    x = game.bee.x - shadowWidth / 2f,
                    y = floorY + 10f
                ),
                size = Size(
                    width = shadowWidth,
                    height = game.bee.radius * 0.34f
                )
            )

            // چرخش زنبور بر اساس سرعت عمودی
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