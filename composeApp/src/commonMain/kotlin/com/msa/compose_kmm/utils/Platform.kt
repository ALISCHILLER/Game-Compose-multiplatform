package com.msa.compose_kmm.utils

/**
 * پلتفرم‌هایی که اپلیکیشن می‌تواند روی آن‌ها اجرا شود.
 *
 * از این enum برای تشخیص محیط اجرای برنامه در کد مشترک استفاده می‌شود.
 */
enum class Platform {

    /**
     * پلتفرم Android.
     */
    Android,

    /**
     * پلتفرم iOS.
     */
    Ios,

    /**
     * پلتفرم Desktop مثل Windows، macOS یا Linux.
     */
    Desktop,

    /**
     * پلتفرم Web.
     */
    Web
}

/**
 * دریافت پلتفرم فعلی در زمان اجرا.
 *
 * پیاده‌سازی واقعی این تابع باید در source set مخصوص هر پلتفرم
 * با actual نوشته شود.
 */
expect fun getPlatform(): Platform