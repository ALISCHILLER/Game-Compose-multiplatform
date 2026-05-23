package com.msa.compose_kmm.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

/**
 * موتور اصلی بازی.
 *
 * این کلاس مستقل از UI طراحی شده است.
 * یعنی Canvas، دکمه، تصویر، صدا و Compose نباید داخل منطق بازی باشند.
 *
 * وظایف این کلاس:
 * - نگهداری state بازی
 * - فیزیک زنبور
 * - تولید و حرکت لوله‌ها
 * - امتیازدهی
 * - تشخیص برخورد
 * - مدیریت شروع/پایان/ریست بازی
 *
 * @param random برای تولید لوله‌ها استفاده می‌شود. تزریق‌پذیر است تا تست‌پذیری بهتر شود.
 */
class Game(
    private val random: Random = Random.Default
) {

    /** عرض فعلی Canvas. */
    var screenWidth by mutableIntStateOf(0)
        private set

    /** ارتفاع فعلی Canvas. */
    var screenHeight by mutableIntStateOf(0)
        private set

    /** وضعیت فعلی بازی. */
    var status by mutableStateOf(GameStatus.Idle)
        private set

    /** امتیاز دور فعلی. */
    var score by mutableIntStateOf(0)
        private set

    /** بهترین امتیاز تا زمانی که برنامه باز است. */
    var bestScore by mutableIntStateOf(0)
        private set

    /** سرعت عمودی زنبور. مقدار منفی یعنی حرکت به سمت بالا. */
    var beeVelocity by mutableStateOf(0f)
        private set

    /** مدل فعلی زنبور. */
    var bee by mutableStateOf(Bee(x = 0f, y = 0f))
        private set

    /** لیست لوله‌های فعال داخل world بازی. */
    var pipePairs by mutableStateOf(emptyList<PipePair>())
        private set

    /** ارتفاع زمین پایین صفحه. */
    val groundHeight: Float
        get() = (screenHeight * GameConfig.GROUND_HEIGHT_RATIO)
            .coerceIn(GameConfig.MIN_GROUND_HEIGHT, GameConfig.MAX_GROUND_HEIGHT)

    /** سقف سرعت زنبور برای جلوگیری از رفتار شدید در افت فریم. */
    val beeMaxVelocity: Float
        get() = (screenHeight * GameConfig.MAX_VELOCITY_RATIO)
            .coerceIn(GameConfig.MIN_MAX_VELOCITY, GameConfig.MAX_MAX_VELOCITY)

    /** فاصله بین لوله بالا و پایین. */
    val pipeGapSize: Float
        get() = (screenHeight * GameConfig.PIPE_GAP_RATIO)
            .coerceIn(GameConfig.MIN_PIPE_GAP, GameConfig.MAX_PIPE_GAP)

    /** عرض لوله‌ها. */
    val pipeWidth: Float
        get() = (screenWidth * GameConfig.PIPE_WIDTH_RATIO)
            .coerceIn(GameConfig.MIN_PIPE_WIDTH, GameConfig.MAX_PIPE_WIDTH)

    /** فاصله افقی بین جفت لوله‌ها. */
    private val pipeSpacing: Float
        get() = (screenWidth * GameConfig.PIPE_SPACING_RATIO)
            .coerceIn(GameConfig.MIN_PIPE_SPACING, GameConfig.MAX_PIPE_SPACING)

    /** گرانش بازی. */
    private val gravity: Float
        get() = (screenHeight * GameConfig.GRAVITY_RATIO)
            .coerceIn(GameConfig.MIN_GRAVITY, GameConfig.MAX_GRAVITY)

    /** نیروی پرش. مقدار منفی است چون محور y به سمت پایین زیاد می‌شود. */
    private val jumpImpulse: Float
        get() = -(screenHeight * GameConfig.JUMP_IMPULSE_RATIO)
            .coerceIn(GameConfig.MIN_JUMP_IMPULSE, GameConfig.MAX_JUMP_IMPULSE)

    /** سرعت حرکت لوله‌ها. */
    private val pipeSpeed: Float
        get() = (screenWidth * GameConfig.PIPE_SPEED_RATIO)
            .coerceIn(GameConfig.MIN_PIPE_SPEED, GameConfig.MAX_PIPE_SPEED)

    /** آیا بازی آماده دریافت دستور پرش است؟ */
    val canReceiveJump: Boolean
        get() = status == GameStatus.Idle || status == GameStatus.Started

    /**
     * به‌روزرسانی اندازه صفحه.
     *
     * در Compose اندازه Canvas بعد از composition مشخص می‌شود.
     * برای همین world بازی فقط وقتی ساخته می‌شود که width/height معتبر داشته باشیم.
     */
    fun updateBounds(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        val changed = width != screenWidth || height != screenHeight
        if (!changed) return

        screenWidth = width
        screenHeight = height

        resetWorld()

        // اگر وسط بازی ابعاد تغییر کرد، بازی را به حالت آماده برمی‌گردانیم.
        if (status == GameStatus.Started) {
            status = GameStatus.Idle
        }
    }

    /** شروع بازی. */
    fun start() {
        if (screenWidth <= 0 || screenHeight <= 0) return

        resetWorld()
        status = GameStatus.Started
    }

    /** شروع مجدد بازی بعد از Game Over. */
    fun restart() {
        start()
    }

    /**
     * پرش زنبور.
     *
     * فقط زمانی اثر دارد که بازی Started باشد.
     */
    fun jump() {
        if (status != GameStatus.Started) return

        beeVelocity = jumpImpulse
    }

    /**
     * update اصلی world بازی.
     *
     * @param deltaMillis زمان سپری‌شده از فریم قبل
     */
    fun update(deltaMillis: Long) {
        if (status != GameStatus.Started) return
        if (screenWidth <= 0 || screenHeight <= 0) return

        val step = (deltaMillis / GameConfig.FRAME_TIME_60_FPS)
            .coerceIn(GameConfig.MIN_FRAME_STEP, GameConfig.MAX_FRAME_STEP)

        updateBee(step)
        updatePipes(step)
        updateScore()
        checkCollisions()
    }

    /** ساخت world اولیه. */
    private fun resetWorld() {
        score = 0
        beeVelocity = 0f

        val radius = (screenWidth * GameConfig.BEE_RADIUS_RATIO)
            .coerceIn(GameConfig.MIN_BEE_RADIUS, GameConfig.MAX_BEE_RADIUS)

        bee = Bee(
            x = screenWidth * GameConfig.BEE_START_X_RATIO,
            y = screenHeight * GameConfig.BEE_START_Y_RATIO,
            radius = radius
        )

        pipePairs = createInitialPipes()
    }

    /** اعمال گرانش و سرعت روی زنبور. */
    private fun updateBee(step: Float) {
        beeVelocity = (beeVelocity + gravity * step)
            .coerceIn(-beeMaxVelocity, beeMaxVelocity)

        bee = bee.copy(y = bee.y + beeVelocity * step)

        // برخورد با سقف Game Over نیست؛ فقط زنبور را نگه می‌داریم.
        if (bee.y - bee.radius <= 0f) {
            bee = bee.copy(y = bee.radius)
            beeVelocity = 0f
        }
    }

    /** تولید لوله‌های اولیه. */
    private fun createInitialPipes(): List<PipePair> {
        val startX = screenWidth + pipeSpacing * GameConfig.FIRST_PIPE_OFFSET_RATIO

        return List(GameConfig.INITIAL_PIPE_COUNT) { index ->
            createPipe(x = startX + index * pipeSpacing)
        }
    }

    /** تولید یک جفت لوله با gap کنترل‌شده. */
    private fun createPipe(x: Float): PipePair {
        val playableBottom = (screenHeight - groundHeight).coerceAtLeast(1f)

        val safeTopMargin = (playableBottom * GameConfig.TOP_SAFE_MARGIN_RATIO)
            .coerceIn(GameConfig.MIN_TOP_SAFE_MARGIN, GameConfig.MAX_TOP_SAFE_MARGIN)

        val safeBottomMargin = (playableBottom * GameConfig.BOTTOM_SAFE_MARGIN_RATIO)
            .coerceIn(GameConfig.MIN_BOTTOM_SAFE_MARGIN, GameConfig.MAX_BOTTOM_SAFE_MARGIN)

        val minTopHeight = safeTopMargin

        val maxTopHeight = (playableBottom - safeBottomMargin - pipeGapSize)
            .coerceAtLeast(minTopHeight + 1f)

        val topHeight = if (maxTopHeight <= minTopHeight) {
            (playableBottom * 0.34f).coerceAtLeast(minTopHeight)
        } else {
            minTopHeight + (maxTopHeight - minTopHeight) * random.nextFloat()
        }

        return PipePair(
            x = x,
            topHeight = topHeight,
            gapHeight = pipeGapSize,
            width = pipeWidth
        )
    }

    /** حرکت لوله‌ها و تولید لوله جدید. */
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

    /** ثبت امتیاز بعد از عبور کامل از لوله. */
    private fun updateScore() {
        pipePairs = pipePairs.map { pipe ->
            val rightEdge = pipe.x + pipe.width / 2f

            if (!pipe.scored && bee.x > rightEdge) {
                score += 1
                bestScore = maxOf(bestScore, score)
                pipe.copy(scored = true)
            } else {
                pipe
            }
        }
    }

    /** بررسی برخورد با زمین و لوله‌ها. */
    private fun checkCollisions() {
        val floorY = screenHeight - groundHeight
        val beeBounds = bee.toCollisionBounds()

        if (bee.y + bee.radius >= floorY) {
            bee = bee.copy(y = floorY - bee.radius)
            gameOver()
            return
        }

        pipePairs.forEach { pipe ->
            val pipeLeft = pipe.x - pipe.width / 2f
            val pipeRight = pipe.x + pipe.width / 2f

            val overlapX = beeBounds.right >= pipeLeft && beeBounds.left <= pipeRight
            val hitPipe = beeBounds.top <= pipe.topHeight || beeBounds.bottom >= pipe.gapBottom

            if (overlapX && hitPipe) {
                gameOver()
                return
            }
        }
    }

    /** پایان بازی. */
    private fun gameOver() {
        status = GameStatus.Over
        bestScore = maxOf(bestScore, score)
    }
}