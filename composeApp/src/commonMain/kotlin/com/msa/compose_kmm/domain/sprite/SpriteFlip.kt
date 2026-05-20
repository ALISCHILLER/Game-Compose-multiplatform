package com.msa.compose_kmm.domain.sprite

import androidx.compose.runtime.Immutable

/**
 * حالت‌های مختلف برعکس‌کردن Sprite هنگام رسم روی Canvas.
 *
 * در نسخه حرفه‌ای، به جای nullable بودن مقدار flip، از [None] استفاده شده است.
 * این کار باعث می‌شود API تمیزتر، خواناتر و قابل پیش‌بینی‌تر باشد.
 *
 * @property scaleX مقدار scale روی محور افقی
 * @property scaleY مقدار scale روی محور عمودی
 */
@Immutable
enum class SpriteFlip(
    val scaleX: Float,
    val scaleY: Float
) {

    /**
     * بدون هیچ‌گونه برعکس‌سازی.
     */
    None(
        scaleX = 1f,
        scaleY = 1f
    ),

    /**
     * برعکس‌سازی افقی.
     *
     * در این حالت تصویر از چپ به راست mirror می‌شود.
     */
    Horizontal(
        scaleX = -1f,
        scaleY = 1f
    ),

    /**
     * برعکس‌سازی عمودی.
     *
     * در این حالت تصویر از بالا به پایین mirror می‌شود.
     */
    Vertical(
        scaleX = 1f,
        scaleY = -1f
    ),

    /**
     * برعکس‌سازی هم‌زمان افقی و عمودی.
     */
    Both(
        scaleX = -1f,
        scaleY = -1f
    )
}