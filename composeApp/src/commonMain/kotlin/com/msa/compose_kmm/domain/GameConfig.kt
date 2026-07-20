package com.msa.compose_kmm.domain

/**
 * تنظیمات مرکزی و مستقل از دستگاه برای موتور بازی.
 *
 * تمام مقادیر در یک فضای منطقی ثابت تعریف شده‌اند؛ بنابراین Density، اندازه‌ی واقعی
 * Canvas و Refresh Rate نباید درجه سختی بازی را تغییر دهند.
 */
object GameConfig {
    const val WORLD_WIDTH = 360f
    const val WORLD_HEIGHT = 640f

    const val GROUND_HEIGHT = 96f
    const val BEE_RADIUS = 20f
    const val BEE_START_X = 100f
    const val BEE_START_Y = 270f

    const val MAX_VERTICAL_VELOCITY = 560f
    const val GRAVITY_PER_SECOND = 1050f
    const val JUMP_VELOCITY = -360f

    const val PIPE_GAP = 176f
    const val PIPE_WIDTH = 72f
    const val PIPE_SPACING = 228f
    const val PIPE_SPEED_PER_SECOND = 125f
    const val INITIAL_PIPE_COUNT = 3
    const val FIRST_PIPE_OFFSET = 170f
    const val TOP_SAFE_MARGIN = 72f
    const val BOTTOM_SAFE_MARGIN = 64f
    const val PIPE_DESPAWN_MARGIN = 48f

    const val COLLISION_HORIZONTAL_RADIUS_RATIO = 0.66f
    const val COLLISION_VERTICAL_RADIUS_RATIO = 0.70f

    /** شبیه‌سازی 120Hz با زمان صحیح برای جلوگیری از خطای تجمع اعشاری. */
    const val FIXED_TIME_STEP_NANOS = 8_333_333L
    const val FIXED_TIME_STEP_SECONDS = FIXED_TIME_STEP_NANOS / 1_000_000_000f
    const val MAX_FRAME_DELTA_MILLIS = 250L
    const val MAX_FRAME_DELTA_NANOS = MAX_FRAME_DELTA_MILLIS * 1_000_000L
    const val MAX_UPDATES_PER_FRAME = 30
}
