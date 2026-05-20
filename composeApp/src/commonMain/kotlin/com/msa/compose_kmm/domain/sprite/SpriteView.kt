package com.msa.compose_kmm.domain.sprite

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/**
 * نسخه Responsive برای نمایش Sprite.
 *
 * این Composable عرض موجود در layout را می‌خواند و بر اساس آن،
 * Sprite Sheet مناسب را از [SpriteSpec] انتخاب می‌کند.
 *
 * استفاده از این Composable برای اکثر حالت‌ها پیشنهاد می‌شود.
 *
 * @param modifier modifier بیرونی برای کنترل جایگاه و layout
 * @param spriteState وضعیت اجرایی انیمیشن
 * @param spriteSpec مشخصات Sprite Sheetها برای اندازه‌های مختلف صفحه
 * @param animationSpec تنظیمات انیمیشن
 * @param spriteFlip حالت برعکس‌سازی تصویر
 * @param autoPlay اگر true باشد، انیمیشن به صورت خودکار شروع می‌شود
 * @param validateBounds اگر true باشد، اندازه تصویر با تعداد فریم‌ها بررسی می‌شود
 * @param onAnimationFinished زمانی صدا زده می‌شود که انیمیشن non-loop به پایان برسد
 */
@Composable
fun ResponsiveSpriteView(
    modifier: Modifier = Modifier,
    spriteState: SpriteState,
    spriteSpec: SpriteSpec,
    animationSpec: SpriteAnimationSpec,
    spriteFlip: SpriteFlip = SpriteFlip.None,
    autoPlay: Boolean = true,
    validateBounds: Boolean = true,
    onAnimationFinished: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        SpriteView(
            modifier = Modifier,
            spriteState = spriteState,
            spriteSpec = spriteSpec,
            animationSpec = animationSpec,
            screenWidthDp = maxWidth.value,
            spriteFlip = spriteFlip,
            autoPlay = autoPlay,
            validateBounds = validateBounds,
            onAnimationFinished = onAnimationFinished
        )
    }
}

/**
 * نمایش و انیمیت‌کردن Sprite Sheet روی Canvas.
 *
 * این Composable:
 *
 * - Sprite Sheet مناسب را بر اساس عرض صفحه انتخاب می‌کند
 * - تصویر را از resources بارگذاری می‌کند
 * - فریم فعلی را روی Canvas رسم می‌کند
 * - اجرای انیمیشن را با LaunchedEffect مدیریت می‌کند
 *
 * @param modifier modifier مربوط به Canvas
 * @param spriteState وضعیت اجرایی انیمیشن
 * @param spriteSpec مشخصات Sprite Sheetها
 * @param animationSpec تنظیمات انیمیشن
 * @param screenWidthDp عرض صفحه بر حسب dp برای انتخاب Sprite Sheet مناسب
 * @param spriteFlip حالت برعکس‌سازی تصویر
 * @param autoPlay اگر true باشد، انیمیشن به صورت خودکار شروع می‌شود
 * @param validateBounds اگر true باشد، اندازه تصویر و تعداد فریم‌ها بررسی می‌شود
 * @param onAnimationFinished callback پایان انیمیشن برای حالت non-loop
 */
@Composable
fun SpriteView(
    modifier: Modifier = Modifier,
    spriteState: SpriteState,
    spriteSpec: SpriteSpec,
    animationSpec: SpriteAnimationSpec,
    screenWidthDp: Float,
    spriteFlip: SpriteFlip = SpriteFlip.None,
    autoPlay: Boolean = true,
    validateBounds: Boolean = true,
    onAnimationFinished: () -> Unit = {}
) {
    require(spriteState.totalFrames == animationSpec.totalFrames) {
        "تعداد فریم‌های SpriteState و SpriteAnimationSpec باید برابر باشند. " +
                "spriteState.totalFrames=${spriteState.totalFrames}, " +
                "animationSpec.totalFrames=${animationSpec.totalFrames}"
    }

    /**
     * انتخاب Sprite Sheet مناسب بر اساس عرض صفحه.
     *
     * با remember از محاسبه مجدد غیرضروری جلوگیری می‌شود.
     */
    val selectedSheet = remember(
        spriteSpec,
        screenWidthDp
    ) {
        spriteSpec.sheetFor(screenWidthDp)
    }

    /**
     * بارگذاری تصویر انتخاب‌شده از منابع پروژه.
     */
    val image = imageResource(selectedSheet.image)

    /**
     * بررسی اینکه Sprite Sheet واقعاً فضای کافی برای تعداد فریم‌های تعریف‌شده دارد.
     *
     * این اعتبارسنجی در زمان توسعه بسیار کمک‌کننده است چون اگر frameWidth،
     * frameHeight، totalFrames یا framesPerRow اشتباه باشند، سریع مشخص می‌شود.
     */
    if (validateBounds) {
        validateSpriteBitmapBounds(
            image = image,
            sheet = selectedSheet,
            animationSpec = animationSpec
        )
    }

    /**
     * effect مربوط به اجرای انیمیشن.
     *
     * این بخش lifecycle-safe است و با خروج Composable از Composition،
     * به صورت خودکار متوقف می‌شود.
     */
    SpriteAnimationEffect(
        spriteState = spriteState,
        animationSpec = animationSpec,
        autoPlay = autoPlay,
        onAnimationFinished = onAnimationFinished
    )

    val density = LocalDensity.current

    /**
     * تبدیل اندازه فریم از px به dp برای تعیین اندازه Canvas.
     */
    val widthDp = with(density) {
        selectedSheet.frameWidthPx.toDp()
    }

    val heightDp = with(density) {
        selectedSheet.frameHeightPx.toDp()
    }

    /**
     * Canvas اصلی برای رسم Sprite.
     *
     * اندازه Canvas برابر اندازه یک فریم است.
     */
    Canvas(
        modifier = modifier.size(
            width = widthDp,
            height = heightDp
        )
    ) {
        drawSpriteFrame(
            image = image,
            sheet = selectedSheet,
            animationSpec = animationSpec,
            frame = spriteState.currentFrame,
            offset = IntOffset.Zero,
            dstSize = IntSize(
                width = size.width.roundToInt(),
                height = size.height.roundToInt()
            ),
            spriteFlip = spriteFlip
        )
    }
}

/**
 * رسم یک فریم مشخص از Sprite Sheet داخل DrawScope.
 *
 * این تابع برای زمانی مناسب است که خودت Canvas سفارشی داری و می‌خواهی
 * Sprite را در کنار اجزای دیگر مثل background، particle، object، enemy،
 * player یا effect رسم کنی.
 *
 * @param image تصویر کامل Sprite Sheet
 * @param sheet مشخصات Sprite Sheet
 * @param animationSpec تنظیمات انیمیشن و چیدمان فریم‌ها
 * @param frame شماره فریمی که باید رسم شود
 * @param offset محل رسم فریم داخل Canvas
 * @param dstSize اندازه مقصد برای رسم فریم
 * @param spriteFlip حالت برعکس‌سازی تصویر
 */
fun DrawScope.drawSpriteFrame(
    image: ImageBitmap,
    sheet: SpriteSheet,
    animationSpec: SpriteAnimationSpec,
    frame: Int,
    offset: IntOffset = IntOffset.Zero,
    dstSize: IntSize = sheet.frameSize,
    spriteFlip: SpriteFlip = SpriteFlip.None
) {
    /**
     * فریم ورودی را به بازه معتبر محدود می‌کنیم
     * تا اگر مقدار اشتباه ارسال شد، از crash یا محاسبه غلط جلوگیری شود.
     */
    val safeFrame = animationSpec.normalizeFrame(frame)

    /**
     * محاسبه نقطه شروع فریم داخل تصویر اصلی Sprite Sheet.
     *
     * مثلا اگر frameWidth=64 و ستون فریم برابر 2 باشد،
     * x برابر 128 خواهد شد.
     */
    val sourceOffset = IntOffset(
        x = animationSpec.columnOf(safeFrame) * sheet.frameWidthPx,
        y = animationSpec.rowOf(safeFrame) * sheet.frameHeightPx
    )

    /**
     * نقطه مرکزی فریم مقصد.
     *
     * برای flip کردن بهتر است pivot مرکز تصویر باشد تا نتیجه قابل پیش‌بینی‌تر شود.
     */
    val pivot = Offset(
        x = offset.x + dstSize.width / 2f,
        y = offset.y + dstSize.height / 2f
    )

    /**
     * اعمال transform قبل از رسم تصویر.
     *
     * اگر SpriteFlip.None باشد، هیچ scale اضافه‌ای اعمال نمی‌شود.
     */
    withTransform(
        transformBlock = {
            if (spriteFlip != SpriteFlip.None) {
                scale(
                    scaleX = spriteFlip.scaleX,
                    scaleY = spriteFlip.scaleY,
                    pivot = pivot
                )
            }
        }
    ) {
        drawImage(
            image = image,
            srcOffset = sourceOffset,
            srcSize = sheet.frameSize,
            dstOffset = offset,
            dstSize = dstSize
        )
    }
}

/**
 * effect داخلی برای کنترل اجرای انیمیشن.
 *
 * این تابع مسئول اجرای loop زمانی انیمیشن است.
 * از آنجایی که داخل Compose و با LaunchedEffect اجرا می‌شود،
 * lifecycle-safe است و نیازی به CoroutineScope دستی ندارد.
 *
 * @param spriteState وضعیت Sprite
 * @param animationSpec تنظیمات انیمیشن
 * @param autoPlay آیا انیمیشن خودکار شروع شود؟
 * @param onAnimationFinished callback پایان انیمیشن در حالت non-loop
 */
@Composable
private fun SpriteAnimationEffect(
    spriteState: SpriteState,
    animationSpec: SpriteAnimationSpec,
    autoPlay: Boolean,
    onAnimationFinished: () -> Unit
) {
    /**
     * همیشه آخرین نسخه callback را نگه می‌داریم
     * تا در صورت recomposition، callback قدیمی صدا زده نشود.
     */
    val latestOnAnimationFinished by rememberUpdatedState(onAnimationFinished)

    /**
     * شروع خودکار انیمیشن در صورت فعال بودن autoPlay.
     */
    LaunchedEffect(
        spriteState,
        autoPlay
    ) {
        if (autoPlay) {
            spriteState.play()
        }
    }

    /**
     * حلقه اصلی انیمیشن.
     *
     * تا زمانی که spriteState.isRunning برابر true باشد،
     * طبق frameDurationMillis صبر می‌کند و سپس فریم را جلو می‌برد.
     */
    LaunchedEffect(
        spriteState,
        spriteState.isRunning,
        animationSpec
    ) {
        while (spriteState.isRunning) {
            delay(animationSpec.frameDurationMillis)

            val shouldContinue = spriteState.advance(
                loop = animationSpec.loop
            )

            if (!shouldContinue) {
                latestOnAnimationFinished()
            }
        }
    }

    /**
     * هنگام خروج Sprite از Composition، انیمیشن pause می‌شود.
     *
     * این کار از اجرای ناخواسته انیمیشن بعد از حذف Composable جلوگیری می‌کند.
     */
    DisposableEffect(spriteState) {
        onDispose {
            spriteState.pause()
        }
    }
}

/**
 * بررسی معتبر بودن اندازه تصویر Sprite Sheet.
 *
 * این تابع بررسی می‌کند که تصویر انتخاب‌شده، فضای کافی برای تمام فریم‌های
 * تعریف‌شده در [SpriteAnimationSpec] را داشته باشد.
 *
 * مثلا اگر:
 *
 * - frameWidthPx = 64
 * - framesPerRow = 4
 *
 * حداقل عرض تصویر باید 256 پیکسل باشد.
 *
 * @param image تصویر بارگذاری‌شده Sprite Sheet
 * @param sheet مشخصات Sprite Sheet
 * @param animationSpec تنظیمات انیمیشن
 */
private fun validateSpriteBitmapBounds(
    image: ImageBitmap,
    sheet: SpriteSheet,
    animationSpec: SpriteAnimationSpec
) {
    val requiredWidth = sheet.frameWidthPx * animationSpec.framesPerRow
    val requiredHeight = sheet.frameHeightPx * animationSpec.requiredRows

    require(requiredWidth <= image.width) {
        "عرض تصویر Sprite Sheet کافی نیست. " +
                "عرض موردنیاز: $requiredWidth px، عرض واقعی تصویر: ${image.width} px"
    }

    require(requiredHeight <= image.height) {
        "ارتفاع تصویر Sprite Sheet کافی نیست. " +
                "ارتفاع موردنیاز: $requiredHeight px، ارتفاع واقعی تصویر: ${image.height} px"
    }
}