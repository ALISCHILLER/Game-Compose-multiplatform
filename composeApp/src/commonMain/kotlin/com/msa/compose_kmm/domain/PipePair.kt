package com.msa.compose_kmm.domain

/**
 * مدل یک لوله.
 *
 * @property x موقعیت افقی مرکز لوله
 * @property topHeight ارتفاع لوله بالایی
 * @property gapHeight ارتفاع فاصله بین دو لوله
 * @property width عرض لوله
 * @property scored آیا امتیاز این لوله گرفته شده یا نه
 */
data class PipePair(
    val x: Float,
    val topHeight: Float,
    val gapHeight: Float,
    val width: Float,
    val scored: Boolean = false
) {
    /**
     * پایین شکاف بین دو لوله.
     */
    val gapBottom: Float
        get() = topHeight + gapHeight
}