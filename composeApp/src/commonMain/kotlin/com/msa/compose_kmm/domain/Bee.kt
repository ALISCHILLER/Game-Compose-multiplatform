package com.msa.compose_kmm.domain

/**
 * مدل داده‌ای مربوط به زنبور داخل بازی.
 *
 * این data class فقط اطلاعات پایه‌ی زنبور را نگه می‌دارد:
 * - موقعیت افقی روی صفحه
 * - موقعیت عمودی روی صفحه
 * - شعاع زنبور برای رسم و تشخیص برخورد
 *
 * چون از data class استفاده شده، Kotlin به‌صورت خودکار متدهایی مثل:
 * copy، equals، hashCode و toString را برای این کلاس می‌سازد.
 *
 * @property x موقعیت افقی زنبور روی صفحه
 * @property y موقعیت عمودی زنبور روی صفحه
 * @property radius شعاع زنبور؛ هم برای رسم دایره و هم برای منطق برخورد قابل استفاده است
 */
data class Bee(
    val x: Float,
    val y: Float,
    val radius: Float = 30f
)