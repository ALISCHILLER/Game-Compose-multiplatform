package com.msa.compose_kmm.domain


/**
 * تنظیمات مرکزی گیم‌پلی.
 *
 * همه‌ی عددهای حساس بازی اینجا جمع شده‌اند تا تغییر درجه سختی، سرعت، فاصله‌ی لوله‌ها
 * و اندازه‌ی کاراکتر بدون دست‌کاری مستقیم منطق اصلی انجام شود.
 *
 * نکته مهم:
 * چون پروژه Multiplatform است، اندازه صفحه در Android/Desktop/Web/iOS متفاوت می‌شود.
 * پس مقدارها تا حد ممکن نسبی تعریف شده‌اند و با coerceIn محدود می‌شوند.
 */
object GameConfig {

    /** نسبت ارتفاع زمین به کل ارتفاع صفحه. */
    const val GROUND_HEIGHT_RATIO = 0.16f

    /** حداقل و حداکثر ارتفاع زمین بر حسب پیکسل منطقی Canvas. */
    const val MIN_GROUND_HEIGHT = 104f
    const val MAX_GROUND_HEIGHT = 168f

    /** نسبت شعاع زنبور به عرض صفحه. */
    const val BEE_RADIUS_RATIO = 0.052f
    const val MIN_BEE_RADIUS = 24f
    const val MAX_BEE_RADIUS = 34f

    /** نسبت موقعیت شروع زنبور به عرض و ارتفاع صفحه. */
    const val BEE_START_X_RATIO = 0.28f
    const val BEE_START_Y_RATIO = 0.42f

    /** سقف سرعت عمودی زنبور. */
    const val MAX_VELOCITY_RATIO = 0.018f
    const val MIN_MAX_VELOCITY = 11.5f
    const val MAX_MAX_VELOCITY = 16.5f

    /** گرانش بازی. */
    const val GRAVITY_RATIO = 0.00078f
    const val MIN_GRAVITY = 0.48f
    const val MAX_GRAVITY = 0.68f

    /** نیروی پرش زنبور. */
    const val JUMP_IMPULSE_RATIO = 0.0142f
    const val MIN_JUMP_IMPULSE = 8.6f
    const val MAX_JUMP_IMPULSE = 12.4f

    /** اندازه شکاف بین لوله بالا و پایین. */
    const val PIPE_GAP_RATIO = 0.285f
    const val MIN_PIPE_GAP = 210f
    const val MAX_PIPE_GAP = 296f

    /** عرض لوله. */
    const val PIPE_WIDTH_RATIO = 0.145f
    const val MIN_PIPE_WIDTH = 76f
    const val MAX_PIPE_WIDTH = 118f

    /** فاصله بین جفت‌لوله‌ها. */
    const val PIPE_SPACING_RATIO = 0.52f
    const val MIN_PIPE_SPACING = 300f
    const val MAX_PIPE_SPACING = 430f

    /** سرعت حرکت لوله‌ها به سمت چپ. */
    const val PIPE_SPEED_RATIO = 0.0082f
    const val MIN_PIPE_SPEED = 4.2f
    const val MAX_PIPE_SPEED = 6.7f

    /** تعداد لوله‌هایی که در شروع بازی ساخته می‌شوند. */
    const val INITIAL_PIPE_COUNT = 3

    /** فاصله اولین لوله از لبه راست صفحه. */
    const val FIRST_PIPE_OFFSET_RATIO = 0.65f

    /** حاشیه امن بالا و پایین برای جلوگیری از سخت‌شدن غیرمنطقی بازی. */
    const val TOP_SAFE_MARGIN_RATIO = 0.14f
    const val BOTTOM_SAFE_MARGIN_RATIO = 0.12f
    const val MIN_TOP_SAFE_MARGIN = 76f
    const val MAX_TOP_SAFE_MARGIN = 132f
    const val MIN_BOTTOM_SAFE_MARGIN = 70f
    const val MAX_BOTTOM_SAFE_MARGIN = 126f

    /** کوچک‌تر کردن collision نسبت به تصویر واقعی برای حس عادلانه‌تر. */
    const val COLLISION_HORIZONTAL_RADIUS_RATIO = 0.66f
    const val COLLISION_VERTICAL_RADIUS_RATIO = 0.70f

    /** نرمال‌سازی delta time نسبت به 60fps. */
    const val FRAME_TIME_60_FPS = 16.6667f
    const val MIN_FRAME_STEP = 0.5f
    const val MAX_FRAME_STEP = 1.8f
}