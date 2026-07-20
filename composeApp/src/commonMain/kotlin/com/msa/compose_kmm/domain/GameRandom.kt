package com.msa.compose_kmm.domain

/**
 * مولد تصادفی کوچک، قطعی و قابل‌ذخیره برای موتور بازی.
 *
 * استفاده از State صریح باعث می‌شود پس از Restore، لوله‌های آینده دقیقاً همان دنباله‌ی
 * قبل از بسته‌شدن برنامه را ادامه دهند. مقدار صفر برای xorshift معتبر نیست و به یک
 * Seed ثابت امن تبدیل می‌شود.
 */
internal class GameRandom(seed: Long) {
    var state: Long = normalize(seed)
        private set

    fun nextFloat(): Float {
        var value = state
        value = value xor (value shl 13)
        value = value xor (value ushr 7)
        value = value xor (value shl 17)
        state = normalize(value)

        val fractionBits = ((state ushr 40) and FLOAT_FRACTION_MASK).toInt()
        return fractionBits / FLOAT_FRACTION_DENOMINATOR
    }

    fun restore(restoredState: Long) {
        state = normalize(restoredState)
    }

    private companion object {
        const val FALLBACK_SEED: Long = 0x4D53414245454C
        const val FLOAT_FRACTION_MASK: Long = 0x00FF_FFFFL
        const val FLOAT_FRACTION_DENOMINATOR: Float = 16_777_216f

        fun normalize(value: Long): Long = if (value == 0L) FALLBACK_SEED else value
    }
}
