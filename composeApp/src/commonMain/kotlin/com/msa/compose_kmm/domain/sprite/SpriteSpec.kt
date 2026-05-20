package com.msa.compose_kmm.domain.sprite

import androidx.compose.runtime.Immutable

/**
 * مشخصات Responsive برای انتخاب Sprite Sheet مناسب بر اساس اندازه صفحه.
 *
 * اگر فقط یک Sprite Sheet داری، کافی است فقط [default] را مقداردهی کنی.
 * اگر برای اندازه‌های مختلف صفحه، تصویرهای متفاوت داری، می‌توانی مقادیر
 * [small]، [normal]، [large] و [tablet] را هم تنظیم کنی.
 *
 * اگر Sprite Sheet مخصوص یک دسته‌بندی وجود نداشته باشد،
 * به صورت خودکار از [default] استفاده می‌شود.
 *
 * @param default Sprite Sheet پیش‌فرض
 * @param small Sprite Sheet مخصوص صفحه‌های کوچک
 * @param normal Sprite Sheet مخصوص صفحه‌های معمولی
 * @param large Sprite Sheet مخصوص صفحه‌های بزرگ
 * @param tablet Sprite Sheet مخصوص تبلت
 */
@Immutable
data class SpriteSpec(
    val default: SpriteSheet,
    val small: SpriteSheet? = null,
    val normal: SpriteSheet? = null,
    val large: SpriteSheet? = null,
    val tablet: SpriteSheet? = null
) {

    /**
     * انتخاب Sprite Sheet مناسب بر اساس عرض صفحه.
     *
     * @param screenWidthDp عرض صفحه بر حسب dp
     * @return مناسب‌ترین Sprite Sheet برای اندازه صفحه
     */
    fun sheetFor(screenWidthDp: Float): SpriteSheet {
        return when (screenWidthDp.toScreenCategory()) {
            ScreenCategory.Small -> small ?: default
            ScreenCategory.Normal -> normal ?: default
            ScreenCategory.Large -> large ?: default
            ScreenCategory.Tablet -> tablet ?: default
        }
    }
}