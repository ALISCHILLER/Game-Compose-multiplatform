package com.msa.compose_kmm.domain

/**
 * محدوده برخورد زنبور.
 *
 * چون تصویر زنبور بال و جزئیات بیرونی دارد، collision دقیقاً برابر تصویر نیست.
 * این کار باعث می‌شود برخوردها منصفانه‌تر باشند و کاربر حس نکند بی‌دلیل باخته است.
 */
data class BeeCollisionBounds(
    val left: Float,
    val right: Float,
    val top: Float,
    val bottom: Float
)

/**
 * تبدیل مدل زنبور به محدوده برخورد.
 */
fun Bee.toCollisionBounds(): BeeCollisionBounds {
    val horizontalRadius = radius * GameConfig.COLLISION_HORIZONTAL_RADIUS_RATIO
    val verticalRadius = radius * GameConfig.COLLISION_VERTICAL_RADIUS_RATIO

    return BeeCollisionBounds(
        left = x - horizontalRadius,
        right = x + horizontalRadius,
        top = y - verticalRadius,
        bottom = y + verticalRadius
    )
}