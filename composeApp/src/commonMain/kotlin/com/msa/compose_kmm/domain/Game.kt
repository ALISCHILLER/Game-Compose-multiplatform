package com.msa.compose_kmm.domain

import kotlin.random.Random

/** موتور خالص و مستقل از UI بازی با Fixed Timestep قطعی. */
class Game private constructor(
    private val random: GameRandom
) {
    constructor(seed: Long = Random.Default.nextLong()) : this(GameRandom(seed))

    constructor(random: Random) : this(random.nextLong())
    val worldWidth: Float = GameConfig.WORLD_WIDTH
    val worldHeight: Float = GameConfig.WORLD_HEIGHT
    val groundHeight: Float = GameConfig.GROUND_HEIGHT
    val beeMaxVelocity: Float = GameConfig.MAX_VERTICAL_VELOCITY
    val pipeSpeedPerSecond: Float = GameConfig.PIPE_SPEED_PER_SECOND

    var status: GameStatus = GameStatus.Idle
        private set

    var score: Int = 0
        private set

    var bestScore: Int = 0
        private set

    var roundStartBestScore: Int = 0
        private set

    var beeVelocity: Float = 0f
        private set

    var bee: Bee = createInitialBee()
        private set

    var pipePairs: List<PipePair> = emptyList()
        private set

    private var accumulatedNanos = 0L

    init {
        resetRound()
    }

    fun start() {
        resetRound()
        status = GameStatus.Started
    }

    fun restart() = start()

    /** پاک‌سازی کامل پیشرفت محلی و بازگشت به حالت اولیه. */
    fun resetProgress() {
        status = GameStatus.Idle
        score = 0
        bestScore = 0
        roundStartBestScore = 0
        beeVelocity = 0f
        accumulatedNanos = 0L
        bee = createInitialBee()
        pipePairs = createInitialPipes()
    }

    fun jump() {
        if (status == GameStatus.Started) {
            beeVelocity = GameConfig.JUMP_VELOCITY
        }
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        status = status,
        score = score,
        bestScore = bestScore,
        roundStartBestScore = roundStartBestScore,
        beeVelocity = beeVelocity,
        bee = bee,
        pipePairs = pipePairs,
        randomState = random.state
    )

    /**
     * Snapshot معتبر را بازگردانی می‌کند. بدهی زمانی عمداً بازیابی نمی‌شود تا بعد از
     * Resume یا Process Recreation جهش فیزیکی ایجاد نشود.
     */
    fun restore(snapshot: GameSnapshot): Boolean {
        if (!isRestorable(snapshot)) return false

        status = snapshot.status
        score = snapshot.score
        bestScore = maxOf(snapshot.bestScore, snapshot.score)
        roundStartBestScore = snapshot.roundStartBestScore.coerceIn(0, bestScore)
        beeVelocity = snapshot.beeVelocity.coerceIn(-beeMaxVelocity, beeMaxVelocity)
        bee = snapshot.bee
        pipePairs = snapshot.pipePairs
        random.restore(snapshot.randomState)
        accumulatedNanos = 0L
        return true
    }

    fun update(deltaMillis: Long): Boolean {
        val safeDeltaMillis = deltaMillis.coerceIn(0L, GameConfig.MAX_FRAME_DELTA_MILLIS)
        return updateNanos(safeDeltaMillis * 1_000_000L)
    }

    fun updateNanos(deltaNanos: Long): Boolean {
        if (status != GameStatus.Started || deltaNanos <= 0L) return false

        accumulatedNanos += deltaNanos.coerceAtMost(GameConfig.MAX_FRAME_DELTA_NANOS)
        var updateCount = 0

        while (
            accumulatedNanos >= GameConfig.FIXED_TIME_STEP_NANOS &&
            updateCount < GameConfig.MAX_UPDATES_PER_FRAME &&
            status == GameStatus.Started
        ) {
            simulateStep(GameConfig.FIXED_TIME_STEP_SECONDS)
            accumulatedNanos -= GameConfig.FIXED_TIME_STEP_NANOS
            updateCount += 1
        }

        if (updateCount == GameConfig.MAX_UPDATES_PER_FRAME) {
            accumulatedNanos = 0L
        }

        return updateCount > 0
    }

    private fun simulateStep(deltaSeconds: Float) {
        updateBee(deltaSeconds)
        updatePipes(deltaSeconds)
        checkCollisions()
        if (status == GameStatus.Started) updateScore()
    }

    private fun resetRound() {
        roundStartBestScore = bestScore
        score = 0
        beeVelocity = 0f
        accumulatedNanos = 0L
        bee = createInitialBee()
        pipePairs = createInitialPipes()
    }

    private fun createInitialBee() = Bee(
        x = GameConfig.BEE_START_X,
        y = GameConfig.BEE_START_Y,
        radius = GameConfig.BEE_RADIUS
    )

    private fun updateBee(deltaSeconds: Float) {
        beeVelocity = (beeVelocity + GameConfig.GRAVITY_PER_SECOND * deltaSeconds)
            .coerceIn(-beeMaxVelocity, beeMaxVelocity)

        bee = bee.copy(y = bee.y + beeVelocity * deltaSeconds)

        if (bee.y - bee.radius <= 0f) {
            bee = bee.copy(y = bee.radius)
            beeVelocity = 0f
        }
    }

    private fun createInitialPipes(): List<PipePair> {
        val firstX = worldWidth + GameConfig.FIRST_PIPE_OFFSET
        return List(GameConfig.INITIAL_PIPE_COUNT) { index ->
            createPipe(firstX + index * GameConfig.PIPE_SPACING)
        }
    }

    private fun createPipe(x: Float): PipePair {
        val playableBottom = worldHeight - groundHeight
        val minTopHeight = GameConfig.TOP_SAFE_MARGIN
        val maxTopHeight = (
            playableBottom - GameConfig.BOTTOM_SAFE_MARGIN - GameConfig.PIPE_GAP
            ).coerceAtLeast(minTopHeight)

        val topHeight = if (maxTopHeight == minTopHeight) {
            minTopHeight
        } else {
            minTopHeight + (maxTopHeight - minTopHeight) * random.nextFloat()
        }

        return PipePair(
            x = x,
            topHeight = topHeight,
            gapHeight = GameConfig.PIPE_GAP,
            width = GameConfig.PIPE_WIDTH
        )
    }

    private fun updatePipes(deltaSeconds: Float) {
        val movement = GameConfig.PIPE_SPEED_PER_SECOND * deltaSeconds
        val movedPipes = pipePairs
            .asSequence()
            .map { pipe -> pipe.copy(x = pipe.x - movement) }
            .filter { pipe -> pipe.x + pipe.width / 2f > -GameConfig.PIPE_DESPAWN_MARGIN }
            .toMutableList()

        val lastX = movedPipes.maxOfOrNull { it.x }
        if (lastX == null) {
            movedPipes += createPipe(worldWidth + GameConfig.PIPE_SPACING)
        } else if (lastX < worldWidth + GameConfig.PIPE_SPACING / 2f) {
            movedPipes += createPipe(lastX + GameConfig.PIPE_SPACING)
        }

        pipePairs = movedPipes
    }

    private fun updateScore() {
        var gainedScore = 0
        pipePairs = pipePairs.map { pipe ->
            val rightEdge = pipe.x + pipe.width / 2f
            if (!pipe.scored && bee.x > rightEdge) {
                gainedScore += 1
                pipe.copy(scored = true)
            } else {
                pipe
            }
        }

        if (gainedScore > 0) {
            score += gainedScore
            bestScore = maxOf(bestScore, score)
        }
    }

    private fun checkCollisions() {
        val floorY = worldHeight - groundHeight
        val beeBounds = bee.toCollisionBounds()

        if (bee.y + bee.radius >= floorY) {
            bee = bee.copy(y = floorY - bee.radius)
            gameOver()
            return
        }

        for (pipe in pipePairs) {
            val pipeLeft = pipe.x - pipe.width / 2f
            val pipeRight = pipe.x + pipe.width / 2f
            val overlapsHorizontally = beeBounds.right >= pipeLeft && beeBounds.left <= pipeRight
            val hitsSolidPipe = beeBounds.top <= pipe.topHeight || beeBounds.bottom >= pipe.gapBottom

            if (overlapsHorizontally && hitsSolidPipe) {
                gameOver()
                return
            }
        }
    }

    private fun gameOver() {
        status = GameStatus.Over
        accumulatedNanos = 0L
        bestScore = maxOf(bestScore, score)
    }

    private fun isRestorable(snapshot: GameSnapshot): Boolean {
        if (snapshot.schemaVersion != GameSnapshot.CURRENT_SCHEMA_VERSION) return false
        if (snapshot.score !in 0..MAX_RESTORABLE_SCORE) return false
        if (snapshot.bestScore !in snapshot.score..MAX_RESTORABLE_SCORE) return false
        if (snapshot.roundStartBestScore !in 0..snapshot.bestScore) return false
        if (!snapshot.beeVelocity.isFinite()) return false
        if (snapshot.beeVelocity !in -beeMaxVelocity..beeMaxVelocity) return false
        if (!snapshot.bee.x.isFinite() || snapshot.bee.x !in 0f..worldWidth) return false
        if (!snapshot.bee.y.isFinite() || snapshot.bee.y !in 0f..worldHeight) return false
        if (!snapshot.bee.radius.isFinite() || snapshot.bee.radius !in 1f..worldWidth) return false
        if (snapshot.randomState == 0L) return false
        if (snapshot.pipePairs.size !in 1..MAX_RESTORABLE_PIPE_COUNT) return false

        val minimumPipeX = -worldWidth * 3f
        val maximumPipeX = worldWidth + GameConfig.PIPE_SPACING * MAX_RESTORABLE_PIPE_COUNT

        return snapshot.pipePairs.all { pipe ->
            pipe.x.isFinite() &&
                pipe.x in minimumPipeX..maximumPipeX &&
                pipe.topHeight.isFinite() &&
                pipe.gapHeight.isFinite() &&
                pipe.width.isFinite() &&
                pipe.topHeight in 0f..(worldHeight - groundHeight) &&
                pipe.gapHeight > 0f &&
                pipe.width > 0f &&
                pipe.gapBottom <= worldHeight - groundHeight
        }
    }

    private companion object {
        const val MAX_RESTORABLE_SCORE = 10_000_000
        const val MAX_RESTORABLE_PIPE_COUNT = 32
    }
}
