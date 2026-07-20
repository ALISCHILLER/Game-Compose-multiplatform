package com.msa.compose_kmm

import com.msa.compose_kmm.domain.Game
import com.msa.compose_kmm.domain.GameConfig
import com.msa.compose_kmm.domain.GameStatus
import com.msa.compose_kmm.ui.ResponsiveWindowClass
import com.msa.compose_kmm.ui.calculateGameViewport
import com.msa.compose_kmm.ui.calculateResponsiveLayout
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineTest {
    @Test
    fun gameStartsWithDeviceIndependentLogicalWorld() {
        val game = Game(Random(1))

        assertEquals(GameConfig.WORLD_WIDTH, game.worldWidth)
        assertEquals(GameConfig.WORLD_HEIGHT, game.worldHeight)
        assertEquals(GameStatus.Idle, game.status)
        assertTrue(game.pipePairs.isNotEmpty())
    }

    @Test
    fun idleGameDoesNotAdvanceSimulation() {
        val game = Game(Random(1))
        val initialBee = game.bee
        val initialPipes = game.pipePairs

        assertFalse(game.updateNanos(16_666_667L))
        assertEquals(initialBee, game.bee)
        assertEquals(initialPipes, game.pipePairs)
    }

    @Test
    fun startAndJumpApplyExpectedState() {
        val game = Game(Random(1))

        game.start()
        game.jump()

        assertEquals(GameStatus.Started, game.status)
        assertEquals(GameConfig.JUMP_VELOCITY, game.beeVelocity)
        assertEquals(0, game.score)
    }

    @Test
    fun fixedTimestepProducesEqualResultAcrossCommonFrameRates() {
        val frameRates = listOf(30, 60, 90, 120, 144, 165, 240)
        val snapshots = frameRates.associateWith(::simulateOneSecondAtFrameRate)
        val reference = snapshots.getValue(120)

        snapshots.forEach { (frameRate, snapshot) ->
            assertTrue(
                abs(snapshot.beeY - reference.beeY) < 0.001f,
                "Bee position differs at ${frameRate}Hz"
            )
            assertTrue(
                abs(snapshot.pipeX - reference.pipeX) < 0.001f,
                "Pipe position differs at ${frameRate}Hz"
            )
            assertEquals(reference.status, snapshot.status)
            assertEquals(reference.score, snapshot.score)
        }
    }

    @Test
    fun hugeFrameDelayIsClampedAndDoesNotCreateUnboundedCatchUp() {
        val game = Game(Random(1)).apply { start() }

        assertTrue(game.updateNanos(Long.MAX_VALUE))
        assertEquals(GameStatus.Started, game.status)
        assertTrue(game.bee.y.isFinite())
        assertTrue(game.beeVelocity.isFinite())
    }

    @Test
    fun beeEventuallyHitsGroundWithoutJumping() {
        val game = Game(Random(1))
        game.start()

        repeat(240) {
            if (game.status == GameStatus.Started) game.updateNanos(16_666_667L)
        }

        assertEquals(GameStatus.Over, game.status)
    }


    @Test
    fun frequentJumpsCauseARealPipeCollisionBeforeReachingGround() {
        val game = Game(Random(7)).apply { start() }

        repeat(3_000) { step ->
            if (game.status != GameStatus.Started) return@repeat
            if (step % 20 == 0) game.jump()
            game.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)
        }

        val groundContactY =
            GameConfig.WORLD_HEIGHT - GameConfig.GROUND_HEIGHT - GameConfig.BEE_RADIUS
        assertEquals(GameStatus.Over, game.status)
        assertTrue(
            game.bee.y < groundContactY - 0.01f,
            "The game should end by pipe collision, not by touching the ground."
        )
    }

    @Test
    fun controlledJumpsCanPassAPipeAndIncreaseScore() {
        val game = Game(Random(7)).apply { start() }

        repeat(3_000) { step ->
            if (game.score > 0 || game.status != GameStatus.Started) return@repeat
            if (step % 66 == 63) game.jump()
            game.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)
        }

        assertTrue(game.score > 0, "A deterministic playable path should score at least once.")
        assertEquals(game.score, game.bestScore)
    }

    @Test
    fun restartResetsRoundState() {
        val game = Game(Random(1))
        game.start()
        repeat(240) {
            if (game.status == GameStatus.Started) game.updateNanos(16_666_667L)
        }

        game.restart()

        assertEquals(GameStatus.Started, game.status)
        assertEquals(0, game.score)
        assertEquals(GameConfig.BEE_START_Y, game.bee.y)
        assertEquals(0f, game.beeVelocity)
    }

    @Test
    fun restartKeepsBestScoreAcrossRounds() {
        val game = Game(Random(7)).apply { start() }

        repeat(3_000) { step ->
            if (game.score > 0 || game.status != GameStatus.Started) return@repeat
            if (step % 66 == 63) game.jump()
            game.updateNanos(GameConfig.FIXED_TIME_STEP_NANOS)
        }
        val achievedBest = game.bestScore

        game.restart()

        assertTrue(achievedBest > 0)
        assertEquals(0, game.score)
        assertEquals(achievedBest, game.bestScore)
    }

    private fun simulateOneSecondAtFrameRate(frameRate: Int): EngineSnapshot {
        val game = Game(Random(7)).apply {
            start()
            jump()
        }
        var previousFrameNanos = 0L

        for (frame in 1..frameRate) {
            val currentFrameNanos = frame * 1_000_000_000L / frameRate
            game.updateNanos(currentFrameNanos - previousFrameNanos)
            previousFrameNanos = currentFrameNanos
        }

        return EngineSnapshot(
            beeY = game.bee.y,
            pipeX = game.pipePairs.first().x,
            score = game.score,
            status = game.status
        )
    }

    private data class EngineSnapshot(
        val beeY: Float,
        val pipeX: Float,
        val score: Int,
        val status: GameStatus
    )
}

class GameViewportTest {
    @Test
    fun portraitViewportIsCenteredAndBottomAligned() {
        val viewport = calculateGameViewport(
            canvasWidth = 1080f,
            canvasHeight = 2400f,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )

        assertEquals(3f, viewport.scale)
        assertEquals(0f, viewport.offsetX)
        assertEquals(480f, viewport.offsetY)
    }

    @Test
    fun landscapeViewportZoomsWithoutStretchingAndKeepsGroundAnchored() {
        val viewport = calculateGameViewport(
            canvasWidth = 800f,
            canvasHeight = 360f,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )
        val basicFitScale = 360f / GameConfig.WORLD_HEIGHT

        assertTrue(viewport.scale > basicFitScale)
        assertTrue(viewport.offsetX > 0f)
        assertTrue(viewport.offsetY < 0f)
        assertEquals(
            360f,
            GameConfig.WORLD_HEIGHT * viewport.scale + viewport.offsetY,
            absoluteTolerance = 0.001f
        )
    }

    @Test
    fun ultraWideLandscapeKeepsTheBeeStartAreaVisible() {
        val viewport = calculateGameViewport(
            canvasWidth = 1280f,
            canvasHeight = 400f,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )
        val beeScreenY = GameConfig.BEE_START_Y * viewport.scale + viewport.offsetY

        assertTrue(beeScreenY > 0f)
        assertTrue(beeScreenY < 400f)
        assertTrue(viewport.scale <= 1280f / GameConfig.WORLD_WIDTH)
    }

    @Test
    fun invalidViewportInputReturnsSafeIdentityTransform() {
        val viewport = calculateGameViewport(
            canvasWidth = 0f,
            canvasHeight = -1f,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )

        assertEquals(1f, viewport.scale)
        assertEquals(0f, viewport.offsetX)
        assertEquals(0f, viewport.offsetY)
    }
}


class ResponsiveLayoutTest {
    @Test
    fun smallPortraitUsesCompactVerticalLayout() {
        val spec = calculateResponsiveLayout(widthDp = 320f, heightDp = 568f)

        assertEquals(ResponsiveWindowClass.CompactPortrait, spec.windowClass)
        assertFalse(spec.useHorizontalOverlay)
        assertTrue(spec.useCompactHud)
        assertTrue(spec.stackScoreCards)
        assertTrue(spec.stackControlHints)
        assertTrue(spec.mascotSizeDp <= 100f)
    }

    @Test
    fun lowHeightPhoneLandscapeUsesHorizontalOverlayAndCompactHud() {
        val spec = calculateResponsiveLayout(widthDp = 800f, heightDp = 360f)

        assertEquals(ResponsiveWindowClass.CompactLandscape, spec.windowClass)
        assertTrue(spec.useHorizontalOverlay)
        assertTrue(spec.useCompactHud)
        assertFalse(spec.stackScoreCards)
        assertTrue(spec.outerVerticalPaddingDp <= 8f)
        assertFalse(spec.stackControlHints)
        assertTrue(spec.actionMaxWidthDp <= 300f)
    }

    @Test
    fun portraitTabletUsesWiderCenteredPanelWithoutHorizontalOverlay() {
        val spec = calculateResponsiveLayout(widthDp = 800f, heightDp = 1280f)

        assertEquals(ResponsiveWindowClass.MediumPortrait, spec.windowClass)
        assertFalse(spec.useHorizontalOverlay)
        assertTrue(spec.panelMaxWidthDp >= 560f)
        assertFalse(spec.useCompactHud)
        assertTrue(spec.mascotSizeDp >= 110f)
        assertTrue(spec.panelCornerRadiusDp >= 30f)
    }

    @Test
    fun largeFontScaleMakesShortLandscapeCompact() {
        val spec = calculateResponsiveLayout(
            widthDp = 900f,
            heightDp = 580f,
            fontScale = 2f
        )

        assertTrue(spec.useHorizontalOverlay)
        assertTrue(spec.useCompactHud)
    }
}
