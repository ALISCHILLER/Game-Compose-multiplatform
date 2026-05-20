package com.msa.compose_kmm.domain.sprite

import androidx.compose.runtime.Immutable

/**
 * دسته‌بندی اندازه صفحه برای انتخاب Sprite Sheet مناسب.
 *
 * این enum کمک می‌کند بر اساس عرض صفحه، تصویر Sprite مناسب‌تری انتخاب شود.
 * برای مثال می‌توان برای موبایل کوچک از Sprite Sheet سبک‌تر و برای تبلت
 * از Sprite Sheet با کیفیت بالاتر استفاده کرد.
 *
 * بازه‌های پیشنهادی:
 *
 * - [Small]  از 0 تا کمتر از 360dp
 * - [Normal] از 360 تا کمتر از 600dp
 * - [Large]  از 600 تا کمتر از 800dp
 * - [Tablet] از 800dp به بالا
 */
@Immutable
enum class ScreenCategory {

    /**
     * مناسب برای موبایل‌های کوچک.
     */
    Small,

    /**
     * مناسب برای اکثر موبایل‌های معمولی.
     */
    Normal,

    /**
     * مناسب برای موبایل‌های بزرگ یا صفحه‌های عریض‌تر.
     */
    Large,

    /**
     * مناسب برای تبلت‌ها و صفحه‌های بسیار بزرگ.
     */
    Tablet
}

/**
 * تبدیل عرض صفحه بر حسب dp به دسته‌بندی مناسب صفحه.
 *
 * نکته:
 * اگر مقدار منفی ارسال شود، برای جلوگیری از خطا، مقدار حداقل 0 در نظر گرفته می‌شود.
 *
 * @receiver عرض صفحه بر حسب dp
 * @return دسته‌بندی مناسب صفحه
 */
fun Float.toScreenCategory(): ScreenCategory {
    val safeWidth = coerceAtLeast(0f)

    return when {
        safeWidth < 360f -> ScreenCategory.Small
        safeWidth < 600f -> ScreenCategory.Normal
        safeWidth < 800f -> ScreenCategory.Large
        else -> ScreenCategory.Tablet
    }
}