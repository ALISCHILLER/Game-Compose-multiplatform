package com.msa.compose_kmm.domain.sprite

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.DrawableResource

/**
 * مشخصات فیزیکی یک Sprite Sheet.
 *
 * Sprite Sheet یک تصویر واحد است که چندین فریم انیمیشن داخل آن قرار گرفته‌اند.
 * هر فریم بخشی از تصویر اصلی است و با نمایش پشت‌سرهم این فریم‌ها،
 * انیمیشن ساخته می‌شود.
 *
 * مثال:
 *
 * یک Sprite Sheet با 8 فریم و 4 فریم در هر ردیف می‌تواند به شکل زیر باشد:
 *
 * ```
 * ┌────┬────┬────┬────┐
 * │  1 │  2 │  3 │  4 │
 * ├────┼────┼────┼────┤
 * │  5 │  6 │  7 │  8 │
 * └────┴────┴────┴────┘
 * ```
 *
 * @param image تصویر Sprite Sheet از منابع پروژه
 * @param frameWidthPx عرض هر فریم بر حسب پیکسل
 * @param frameHeightPx ارتفاع هر فریم بر حسب پیکسل
 */
@Immutable
data class SpriteSheet(
    val image: DrawableResource,
    val frameWidthPx: Int,
    val frameHeightPx: Int
) {

    init {
        require(frameWidthPx > 0) {
            "frameWidthPx باید بزرگ‌تر از صفر باشد. مقدار فعلی: $frameWidthPx"
        }

        require(frameHeightPx > 0) {
            "frameHeightPx باید بزرگ‌تر از صفر باشد. مقدار فعلی: $frameHeightPx"
        }
    }

    /**
     * اندازه هر فریم به صورت [IntSize].
     *
     * این مقدار برای srcSize و dstSize در drawImage استفاده می‌شود.
     */
    val frameSize: IntSize
        get() = IntSize(
            width = frameWidthPx,
            height = frameHeightPx
        )
}