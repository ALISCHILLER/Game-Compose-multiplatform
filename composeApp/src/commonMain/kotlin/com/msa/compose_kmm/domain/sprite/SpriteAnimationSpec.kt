package com.msa.compose_kmm.domain.sprite

import androidx.compose.runtime.Immutable

/**
 * تنظیمات انیمیشن Sprite.
 *
 * این کلاس رفتار زمانی و ساختار فریم‌های Sprite Sheet را مشخص می‌کند.
 * خود تصویر در [SpriteSheet] تعریف می‌شود و رفتار انیمیشن در این کلاس قرار می‌گیرد.
 *
 * @param totalFrames تعداد کل فریم‌های معتبر انیمیشن
 * @param framesPerRow تعداد فریم‌ها در هر ردیف Sprite Sheet
 * @param frameDurationMillis مدت زمان نمایش هر فریم بر حسب میلی‌ثانیه
 * @param loop اگر true باشد، بعد از رسیدن به آخرین فریم، انیمیشن از ابتدا شروع می‌شود
 */
@Immutable
data class SpriteAnimationSpec(
    val totalFrames: Int,
    val framesPerRow: Int,
    val frameDurationMillis: Long = 50L,
    val loop: Boolean = true
) {

    init {
        require(totalFrames > 0) {
            "totalFrames باید بزرگ‌تر از صفر باشد. مقدار فعلی: $totalFrames"
        }

        require(framesPerRow > 0) {
            "framesPerRow باید بزرگ‌تر از صفر باشد. مقدار فعلی: $framesPerRow"
        }

        require(frameDurationMillis > 0L) {
            "frameDurationMillis باید بزرگ‌تر از صفر باشد. مقدار فعلی: $frameDurationMillis"
        }
    }

    /**
     * تعداد ردیف‌هایی که برای نگهداری تمام فریم‌ها لازم است.
     *
     * مثال:
     * اگر totalFrames برابر 10 و framesPerRow برابر 4 باشد،
     * تعداد ردیف موردنیاز برابر 3 خواهد بود.
     */
    val requiredRows: Int
        get() = (totalFrames + framesPerRow - 1) / framesPerRow

    /**
     * محدودکردن شماره فریم به بازه معتبر.
     *
     * اگر مقدار کمتر از صفر باشد، صفر برمی‌گردد.
     * اگر مقدار بیشتر از آخرین فریم باشد، آخرین فریم برمی‌گردد.
     *
     * @param frame شماره فریم ورودی
     * @return شماره فریم معتبر
     */
    fun normalizeFrame(frame: Int): Int {
        return frame.coerceIn(
            minimumValue = 0,
            maximumValue = totalFrames - 1
        )
    }

    /**
     * محاسبه شماره ردیف فریم داخل Sprite Sheet.
     *
     * @param frame شماره فریم
     * @return شماره ردیف فریم
     */
    fun rowOf(frame: Int): Int {
        return normalizeFrame(frame) / framesPerRow
    }

    /**
     * محاسبه شماره ستون فریم داخل Sprite Sheet.
     *
     * @param frame شماره فریم
     * @return شماره ستون فریم
     */
    fun columnOf(frame: Int): Int {
        return normalizeFrame(frame) % framesPerRow
    }
}