package com.msa.compose_kmm.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * کلاس اصلی مدیریت منطق بازی.
 *
 * این کلاس مسئول نگهداری و کنترل وضعیت‌های مهم بازی است؛ مثل:
 * - وضعیت فعلی بازی
 * - سرعت عمودی زنبور
 * - موقعیت فعلی زنبور
 * - اعمال گرانش روی زنبور
 * - اجرای پرش زنبور
 *
 * از mutableStateOf استفاده شده تا وقتی مقدارهایی مثل bee، status یا beeVelocity تغییر می‌کنند،
 * Jetpack Compose بتواند UI را دوباره رسم کند.
 *
 * @property screenWith عرض صفحه‌ی بازی
 * @property screenHeight ارتفاع صفحه‌ی بازی
 * @property gravity مقدار گرانش؛ هرچه بیشتر باشد زنبور سریع‌تر به سمت پایین حرکت می‌کند
 * @property beeJumpImpulse نیروی پرش زنبور؛ مقدار منفی باعث حرکت به سمت بالا می‌شود
 * @property beeMaxVelocity بیشترین سرعت مجاز زنبور برای جلوگیری از زیاد شدن بیش از حد سرعت
 */
data class Game(
    val screenWith: Int =0,
    val screenHeight: Int= 0,
    val gravity: Float = 0.7f,
    val beeJumpImpulse: Float = -12f,
    val beeMaxVelocity: Float = 25f,
) {

    /**
     * وضعیت فعلی بازی.
     *
     * مقدار اولیه Idle است؛ یعنی بازی هنوز در حالت شروع نشده قرار دارد.
     *
     * private set یعنی از بیرون کلاس فقط می‌توان مقدار status را خواند،
     * اما تغییر دادن آن فقط از داخل همین کلاس ممکن است.
     */
    var status by mutableStateOf(GameStatus.Idle)
        private set

    /**
     * سرعت عمودی فعلی زنبور.
     *
     * مقدار مثبت یعنی زنبور به سمت پایین حرکت می‌کند.
     * مقدار منفی یعنی زنبور به سمت بالا حرکت می‌کند.
     *
     * این مقدار با گذشت زمان و اعمال gravity تغییر می‌کند.
     */
    var beeVelocity by mutableStateOf(0f)
        private set

    /**
     * آبجکت زنبور داخل بازی.
     *
     * در ابتدا زنبور در مرکز صفحه قرار می‌گیرد.
     * چون bee با mutableStateOf تعریف شده، هر بار که مقدار آن تغییر کند،
     * Compose متوجه تغییر می‌شود و بخش مربوط به UI دوباره رسم می‌شود.
     */
    var bee by mutableStateOf(
        Bee(
            x = (screenWith / 2).toFloat(),
            y = (screenHeight / 2).toFloat()
        )
    )

    /**
     * شروع بازی.
     *
     * با صدا زدن این تابع، وضعیت بازی از Idle به Started تغییر می‌کند.
     */
    fun start() {
        status = GameStatus.Started
    }

    /**
     * پایان دادن به بازی.
     *
     * با صدا زدن این تابع، وضعیت بازی روی Over قرار می‌گیرد.
     * بعداً می‌توان از این وضعیت برای نمایش صفحه Game Over یا توقف منطق بازی استفاده کرد.
     */
    fun gameOver() {
        status = GameStatus.Over
    }

    /**
     * اجرای پرش زنبور.
     *
     * با این تابع سرعت عمودی زنبور برابر beeJumpImpulse می‌شود.
     * چون مقدار beeJumpImpulse منفی است، زنبور به سمت بالا حرکت می‌کند.
     */
    fun jump() {
        beeVelocity = beeJumpImpulse
    }

    /**
     * به‌روزرسانی منطق بازی در هر فریم.
     *
     * در این تابع چند کار انجام می‌شود:
     *
     * 1. مقدار gravity به سرعت فعلی زنبور اضافه می‌شود.
     * 2. سرعت زنبور بین -beeMaxVelocity و beeMaxVelocity محدود می‌شود.
     * 3. موقعیت عمودی زنبور با توجه به سرعت جدید تغییر می‌کند.
     *
     * تابع coerceIn باعث می‌شود سرعت زنبور از محدوده‌ی مجاز بیشتر یا کمتر نشود.
     */
    fun updateGameProgress() {
        if (bee.y <0){
            stopTheBee()
            return
        }else if (bee.y > screenHeight){
            gameOver()
            return
        }
        beeVelocity = (beeVelocity + gravity).coerceIn(-beeMaxVelocity, beeMaxVelocity)

        bee = bee.copy(
            y = bee.y + beeVelocity
        )
    }

    fun stopTheBee(){
        beeVelocity = 0f
        bee = bee.copy(y = 0f)
    }
}