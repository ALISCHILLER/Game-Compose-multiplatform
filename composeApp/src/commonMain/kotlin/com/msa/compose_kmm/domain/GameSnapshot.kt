package com.msa.compose_kmm.domain

/**
 * تصویر immutable از وضعیت کامل بازی.
 *
 * این مدل بین موتور، UI، تست‌ها و لایه ذخیره‌سازی مشترک است و هیچ وابستگی‌ای
 * به Compose یا APIهای پلتفرمی ندارد. وضعیت مولد تصادفی و رکورد ابتدای دور نیز
 * ذخیره می‌شوند تا ادامه بازی و تشخیص رکورد جدید پس از Restore قطعی بمانند.
 */
data class GameSnapshot(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val status: GameStatus,
    val score: Int,
    val bestScore: Int,
    val roundStartBestScore: Int = bestScore,
    val beeVelocity: Float,
    val bee: Bee,
    val pipePairs: List<PipePair>,
    val randomState: Long = DEFAULT_RANDOM_STATE
) {
    val isNewRecord: Boolean
        get() = status == GameStatus.Over && score > roundStartBestScore

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 4
        const val DEFAULT_RANDOM_STATE: Long = 0x4D53414245454C
    }
}
