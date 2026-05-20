package com.msa.compose_kmm.domain.sprite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * وضعیت اجرایی انیمیشن Sprite.
 *
 * این کلاس فقط state انیمیشن را نگه می‌دارد و خودش CoroutineScope نمی‌سازد.
 * اجرای زمان‌بندی انیمیشن داخل Composable و با LaunchedEffect انجام می‌شود.
 *
 * این طراحی باعث می‌شود:
 *
 * - مدیریت lifecycle امن‌تر باشد
 * - نیازی به cleanup دستی نباشد
 * - از memory leak جلوگیری شود
 * - کنترل play, pause, stop و seek ساده‌تر باشد
 *
 * @param totalFrames تعداد کل فریم‌های انیمیشن
 * @param initialFrame فریم شروع انیمیشن
 */
@Stable
class SpriteState internal constructor(
    val totalFrames: Int,
    initialFrame: Int = 0
) {

    init {
        require(totalFrames > 0) {
            "totalFrames باید بزرگ‌تر از صفر باشد. مقدار فعلی: $totalFrames"
        }
    }

    /**
     * فریم فعلی انیمیشن.
     *
     * مقدار این property فقط از داخل خود کلاس تغییر می‌کند.
     * برای تغییر فریم از متدهای [seekTo]، [nextFrame] و [previousFrame] استفاده کن.
     */
    var currentFrame: Int by mutableIntStateOf(
        initialFrame.coerceIn(
            minimumValue = 0,
            maximumValue = totalFrames - 1
        )
    )
        private set

    /**
     * مشخص می‌کند انیمیشن در حال اجرا است یا نه.
     */
    var isRunning: Boolean by mutableStateOf(false)
        private set

    /**
     * آیا فریم فعلی، اولین فریم است؟
     */
    val isFirstFrame: Boolean
        get() = currentFrame == 0

    /**
     * آیا فریم فعلی، آخرین فریم است؟
     */
    val isLastFrame: Boolean
        get() = currentFrame == totalFrames - 1

    /**
     * شروع انیمیشن.
     */
    fun play() {
        isRunning = true
    }

    /**
     * توقف موقت انیمیشن بدون ریست‌کردن فریم.
     */
    fun pause() {
        isRunning = false
    }

    /**
     * توقف کامل انیمیشن و برگشت به فریم مشخص‌شده.
     *
     * به صورت پیش‌فرض انیمیشن به فریم صفر برمی‌گردد.
     *
     * @param resetFrame فریمی که بعد از stop باید نمایش داده شود
     */
    fun stop(resetFrame: Int = 0) {
        isRunning = false
        seekTo(resetFrame)
    }

    /**
     * رفتن مستقیم به یک فریم مشخص.
     *
     * اگر مقدار خارج از محدوده باشد، به نزدیک‌ترین مقدار معتبر محدود می‌شود.
     *
     * @param frame شماره فریم مقصد
     */
    fun seekTo(frame: Int) {
        currentFrame = frame.coerceIn(
            minimumValue = 0,
            maximumValue = totalFrames - 1
        )
    }

    /**
     * رفتن به فریم بعدی.
     *
     * @param loop اگر true باشد، بعد از آخرین فریم به فریم اول برمی‌گردد
     * @return اگر انیمیشن بتواند ادامه پیدا کند true، در غیر این صورت false
     */
    fun nextFrame(loop: Boolean = true): Boolean {
        return when {
            currentFrame < totalFrames - 1 -> {
                currentFrame += 1
                true
            }

            loop -> {
                currentFrame = 0
                true
            }

            else -> {
                isRunning = false
                false
            }
        }
    }

    /**
     * برگشت به فریم قبلی.
     *
     * @param loop اگر true باشد، قبل از فریم اول به آخرین فریم می‌رود
     */
    fun previousFrame(loop: Boolean = true) {
        currentFrame = when {
            currentFrame > 0 -> currentFrame - 1
            loop -> totalFrames - 1
            else -> 0
        }
    }

    /**
     * جلو بردن انیمیشن از داخل effect انیمیشن.
     *
     * این متد internal است چون بهتر است بیرون از این پکیج مستقیماً استفاده نشود.
     *
     * @param loop آیا انیمیشن باید loop شود یا نه
     * @return اگر انیمیشن ادامه داشته باشد true، در غیر این صورت false
     */
    internal fun advance(loop: Boolean): Boolean {
        return nextFrame(loop = loop)
    }
}

/**
 * ساخت و نگهداری [SpriteState] در Compose.
 *
 * با تغییر [totalFrames] یا [initialFrame]، state جدید ساخته می‌شود.
 *
 * @param totalFrames تعداد کل فریم‌ها
 * @param initialFrame فریم شروع
 */
@Composable
fun rememberSpriteState(
    totalFrames: Int,
    initialFrame: Int = 0
): SpriteState {
    return remember(
        totalFrames,
        initialFrame
    ) {
        SpriteState(
            totalFrames = totalFrames,
            initialFrame = initialFrame
        )
    }
}