package com.msa.compose_kmm.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

class Game {

    var screenWidth by mutableIntStateOf(0)
        private set

    var screenHeight by mutableIntStateOf(0)
        private set

    var status by mutableStateOf(GameStatus.Idle)
        private set

    var score by mutableIntStateOf(0)
        private set

    var bestScore by mutableIntStateOf(0)
        private set

    var beeVelocity by mutableStateOf(0f)
        private set

    var bee by mutableStateOf(Bee(x = 0f, y = 0f))
        private set

    var pipePairs by mutableStateOf(emptyList<PipePair>())
        private set

    val groundHeight: Float
        get() = (screenHeight * 0.16f).coerceIn(104f, 168f)

    val beeMaxVelocity: Float
        get() = (screenHeight * 0.018f).coerceIn(11.5f, 16.5f)

    val pipeGapSize: Float
        get() = (screenHeight * 0.285f).coerceIn(210f, 296f)

    val pipeWidth: Float
        get() = (screenWidth * 0.145f).coerceIn(76f, 118f)

    private val pipeSpacing: Float
        get() = (screenWidth * 0.52f).coerceIn(300f, 430f)

    private val gravity: Float
        get() = (screenHeight * 0.00078f).coerceIn(0.48f, 0.68f)

    private val jumpImpulse: Float
        get() = -(screenHeight * 0.0142f).coerceIn(8.6f, 12.4f)

    private val pipeSpeed: Float
        get() = (screenWidth * 0.0082f).coerceIn(4.2f, 6.7f)

    fun updateBounds(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        val changed = width != screenWidth || height != screenHeight
        if (!changed) return

        screenWidth = width
        screenHeight = height
        resetWorld()

        if (status == GameStatus.Started) {
            status = GameStatus.Idle
        }
    }

    fun start() {
        if (screenWidth <= 0 || screenHeight <= 0) return

        resetWorld()
        status = GameStatus.Started
    }

    fun restart() {
        start()
    }

    fun jump() {
        if (status != GameStatus.Started) return

        beeVelocity = jumpImpulse
    }

    fun update(deltaMillis: Long) {
        if (status != GameStatus.Started) return
        if (screenWidth <= 0 || screenHeight <= 0) return

        val step = (deltaMillis / 16.6667f).coerceIn(0.5f, 1.8f)

        updateBee(step)
        updatePipes(step)
        updateScore()
        checkCollisions()
    }

    private fun resetWorld() {
        score = 0
        beeVelocity = 0f

        val radius = (screenWidth * 0.052f).coerceIn(24f, 34f)

        bee = Bee(
            x = screenWidth * 0.28f,
            y = screenHeight * 0.42f,
            radius = radius
        )

        pipePairs = createInitialPipes()
    }

    private fun updateBee(step: Float) {
        beeVelocity = (beeVelocity + gravity * step)
            .coerceIn(-beeMaxVelocity, beeMaxVelocity)

        bee = bee.copy(y = bee.y + beeVelocity * step)

        if (bee.y - bee.radius <= 0f) {
            bee = bee.copy(y = bee.radius)
            beeVelocity = 0f
        }
    }

    private fun createInitialPipes(): List<PipePair> {
        val startX = screenWidth + pipeSpacing * 0.65f

        return List(3) { index ->
            createPipe(x = startX + index * pipeSpacing)
        }
    }

    private fun createPipe(x: Float): PipePair {
        val playableBottom = (screenHeight - groundHeight).coerceAtLeast(1f)
        val safeTopMargin = (playableBottom * 0.14f).coerceIn(76f, 132f)
        val safeBottomMargin = (playableBottom * 0.12f).coerceIn(70f, 126f)

        val minTopHeight = safeTopMargin
        val maxTopHeight = (playableBottom - safeBottomMargin - pipeGapSize)
            .coerceAtLeast(minTopHeight + 1f)

        val topHeight = if (maxTopHeight <= minTopHeight) {
            (playableBottom * 0.34f).coerceAtLeast(minTopHeight)
        } else {
            minTopHeight + (maxTopHeight - minTopHeight) * Random.nextFloat()
        }

        return PipePair(
            x = x,
            topHeight = topHeight,
            gapHeight = pipeGapSize,
            width = pipeWidth
        )
    }

    private fun updatePipes(step: Float) {
        val movedPipes = pipePairs
            .map { pipe -> pipe.copy(x = pipe.x - pipeSpeed * step) }
            .filter { pipe -> pipe.x + pipe.width / 2f > -40f }
            .toMutableList()

        if (movedPipes.isEmpty()) {
            movedPipes += createPipe(screenWidth + pipeSpacing)
        } else {
            val lastX = movedPipes.maxOf { it.x }

            if (lastX < screenWidth + pipeSpacing / 2f) {
                movedPipes += createPipe(lastX + pipeSpacing)
            }
        }

        pipePairs = movedPipes
    }

    private fun updateScore() {
        pipePairs = pipePairs.map { pipe ->
            val rightEdge = pipe.x + pipe.width / 2f

            if (!pipe.scored && bee.x > rightEdge) {
                score += 1

                if (score > bestScore) {
                    bestScore = score
                }

                pipe.copy(scored = true)
            } else {
                pipe
            }
        }
    }

    private fun checkCollisions() {
        val floorY = screenHeight - groundHeight

        if (bee.y + bee.radius >= floorY) {
            bee = bee.copy(y = floorY - bee.radius)
            gameOver()
            return
        }

        val horizontalRadius = bee.radius * 0.66f
        val verticalRadius = bee.radius * 0.70f

        val beeLeft = bee.x - horizontalRadius
        val beeRight = bee.x + horizontalRadius
        val beeTop = bee.y - verticalRadius
        val beeBottom = bee.y + verticalRadius

        pipePairs.forEach { pipe ->
            val pipeLeft = pipe.x - pipe.width / 2f
            val pipeRight = pipe.x + pipe.width / 2f

            val overlapX = beeRight >= pipeLeft && beeLeft <= pipeRight
            val hitPipe = beeTop <= pipe.topHeight || beeBottom >= pipe.gapBottom

            if (overlapX && hitPipe) {
                gameOver()
                return
            }
        }
    }

    private fun gameOver() {
        status = GameStatus.Over

        if (score > bestScore) {
            bestScore = score
        }
    }
}