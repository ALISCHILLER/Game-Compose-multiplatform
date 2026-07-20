package com.msa.compose_kmm.domain

/**
 * مدل یک جفت لوله.
 *
 * @property x موقعیت مرکز لوله در محور افقی
 * @property topHeight ارتفاع لوله بالایی
 * @property gapHeight ارتفاع فاصله بین دو لوله
 * @property width عرض لوله
 * @property scored آیا بازیکن امتیاز این لوله را گرفته یا نه
 */
data class PipePair(
    val x: Float,
    val topHeight: Float,
    val gapHeight: Float,
    val width: Float,
    val scored: Boolean = false
) {
    /** موقعیت پایین شکاف بین دو لوله. */
    val gapBottom: Float
        get() = topHeight + gapHeight
}