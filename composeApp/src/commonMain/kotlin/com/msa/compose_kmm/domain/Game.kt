package com.msa.compose_kmm.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

/**
 * هسته اصلی منطق بازی.
 *
 * این کلاس مسئول مدیریت:
 * - وضعیت بازی
 * - زنبور
 * - لوله‌ها
 * - امتیاز
 * - برخوردها
 * - شروع و پایان بازی
 */
class Game(
    private val gravity: Float = 0.55f,
    private val jumpImpulse: Float = -9.5f,
    val beeMaxVelocity: Float = 14f,
    private val pipeSpeed: Float = 5.4f,
    val pipeGapSize: Float = 240f,
    val pipeWidth: Float = 86f,
    private val pipeSpacing: Float = 320f,
    val groundHeight: Float = 140f
) {

    /**
     * عرض صفحه بازی.
     */
    var screenWidth by mutableIntStateOf(0)
        private set

    /**
     * ارتفاع صفحه بازی.
     */
    var screenHeight by mutableIntStateOf(0)
        private set

    /**
     * وضعیت فعلی بازی.
     */
    var status by mutableStateOf(GameStatus.Idle)
        private set

    /**
     * امتیاز فعلی.
     */
    var score by mutableIntStateOf(0)
        private set

    /**
     * بهترین امتیاز.
     *
     * فعلاً فقط داخل حافظه نگه داشته می‌شود.
     */
    var bestScore by mutableIntStateOf(0)
        private set

    /**
     * سرعت عمودی زنبور.
     */
    var beeVelocity by mutableStateOf(0f)
        private set

    /**
     * زنبور فعلی بازی.
     */
    var bee by mutableStateOf(
        Bee(
            x = 0f,
            y = 0f
        )
    )
        private set

    /**
     * لیست لوله‌های فعلی بازی.
     */
    var pipePairs by mutableStateOf(emptyList<PipePair>())
        private set

    /**
     * ثبت اندازه صفحه.
     *
     * اگر سایز صفحه عوض شود، world ریست می‌شود تا محاسبات بازی خراب نشود.
     */
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

    /**
     * شروع بازی.
     */
    fun start() {
        if (screenWidth <= 0 || screenHeight <= 0) return

        resetWorld()
        status = GameStatus.Started
    }

    /**
     * شروع دوباره بازی.
     */
    fun restart() {
        if (screenWidth <= 0 || screenHeight <= 0) return

        resetWorld()
        status = GameStatus.Started
    }

    /**
     * پرش زنبور.
     */
    fun jump() {
        if (status != GameStatus.Started) return
        beeVelocity = jumpImpulse
    }

    /**
     * آپدیت منطق بازی.
     *
     * @param deltaMillis اختلاف زمانی بین این فریم و فریم قبلی
     */
    fun update(deltaMillis: Long) {
        if (status != GameStatus.Started) return
        if (screenWidth <= 0 || screenHeight <= 0) return

        val step = (deltaMillis / 16.6667f).coerceIn(0.75f, 2.2f)

        updateBee(step)
        updatePipes(step)
        updateScore()
        checkCollisions()
    }

    /**
     * ریست کامل world بازی.
     */
    private fun resetWorld() {
        score = 0
        beeVelocity = 0f

        bee = Bee(
            x = screenWidth * 0.28f,
            y = screenHeight * 0.42f
        )

        pipePairs = createInitialPipes()
    }

    /**
     * آپدیت حرکت زنبور.
     */
    private fun updateBee(step: Float) {
        beeVelocity = (beeVelocity + gravity * step)
            .coerceIn(-beeMaxVelocity, beeMaxVelocity)

        bee = bee.copy(
            y = bee.y + beeVelocity * step
        )

        /**
         * برخورد با سقف:
         * اجازه نمی‌دهیم زنبور از بالای صفحه خارج شود.
         */
        if (bee.y - bee.radius <= 0f) {
            bee = bee.copy(y = bee.radius)
            beeVelocity = 0f
        }
    }

    /**
     * ساخت چند لوله اولیه.
     */
    private fun createInitialPipes(): List<PipePair> {
        val startX = screenWidth + 180f

        return List(3) { index ->
            createPipe(
                x = startX + index * pipeSpacing
            )
        }
    }

    /**
     * ساخت یک لوله جدید با ارتفاع تصادفی.
     */
    private fun createPipe(x: Float): PipePair {
        val topMargin = 120f
        val bottomLimit = screenHeight - groundHeight - 120f

        val maxTopHeight = bottomLimit - pipeGapSize
        val randomTopHeight = Random.nextFloat()
            .let { random ->
                topMargin + (maxTopHeight - topMargin) * random
            }

        return PipePair(
            x = x,
            topHeight = randomTopHeight,
            gapHeight = pipeGapSize,
            width = pipeWidth
        )
    }

    /**
     * آپدیت لوله‌ها:
     * - حرکت به سمت چپ
     * - حذف لوله‌های خارج‌شده
     * - اضافه‌کردن لوله‌های جدید
     */
    private fun updatePipes(step: Float) {
        val movedPipes = pipePairs
            .map { pipe ->
                pipe.copy(
                    x = pipe.x - pipeSpeed * step
                )
            }
            .filter { pipe ->
                pipe.x + pipe.width / 2f > -40f
            }
            .toMutableList()

        if (movedPipes.isEmpty()) {
            movedPipes += createPipe(screenWidth + 200f)
        } else {
            val lastX = movedPipes.maxOf { it.x }
            if (lastX < screenWidth + pipeSpacing / 2f) {
                movedPipes += createPipe(lastX + pipeSpacing)
            }
        }

        pipePairs = movedPipes
    }

    /**
     * محاسبه امتیاز.
     *
     * وقتی زنبور از یک لوله عبور کند و آن لوله هنوز امتیاز نداده باشد،
     * امتیاز یک واحد زیاد می‌شود.
     */
    private fun updateScore() {
        val updated = pipePairs.map { pipe ->
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

        pipePairs = updated
    }

    /**
     * بررسی برخوردها:
     * - برخورد با زمین
     * - برخورد با لوله‌ها
     */
    private fun checkCollisions() {
        val floorY = screenHeight - groundHeight

        /**
         * برخورد با زمین.
         */
        if (bee.y + bee.radius >= floorY) {
            bee = bee.copy(y = floorY - bee.radius)
            gameOver()
            return
        }

        /**
         * باکس برخورد تقریبی زنبور.
         * کمی کوچک‌تر از شعاع کامل گرفته شده تا حس برخورد طبیعی‌تر شود.
         */
        val beeLeft = bee.x - bee.radius * 0.72f
        val beeRight = bee.x + bee.radius * 0.72f
        val beeTop = bee.y - bee.radius * 0.72f
        val beeBottom = bee.y + bee.radius * 0.72f

        pipePairs.forEach { pipe ->
            val pipeLeft = pipe.x - pipe.width / 2f
            val pipeRight = pipe.x + pipe.width / 2f
            val gapTop = pipe.topHeight
            val gapBottom = pipe.gapBottom

            val overlapX = beeRight >= pipeLeft && beeLeft <= pipeRight
            val hitUpperPipe = beeTop <= gapTop
            val hitLowerPipe = beeBottom >= gapBottom

            if (overlapX && (hitUpperPipe || hitLowerPipe)) {
                gameOver()
                return
            }
        }
    }

    /**
     * پایان بازی.
     */
    private fun gameOver() {
        status = GameStatus.Over
        if (score > bestScore) {
            bestScore = score
        }
    }
}