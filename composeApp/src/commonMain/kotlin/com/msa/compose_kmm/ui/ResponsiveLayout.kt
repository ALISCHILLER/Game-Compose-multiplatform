package com.msa.compose_kmm.ui

/** Platform-independent window classes used by the game UI. */
enum class ResponsiveWindowClass {
    CompactPortrait,
    CompactLandscape,
    MediumPortrait,
    MediumLandscape,
    Expanded
}

/**
 * All responsive decisions are expressed in dp so Android, iOS, Desktop and Web share one policy.
 */
data class ResponsiveLayoutSpec(
    val windowClass: ResponsiveWindowClass,
    val useHorizontalOverlay: Boolean,
    val useCompactHud: Boolean,
    val stackScoreCards: Boolean,
    val stackControlHints: Boolean,
    val compactHeight: Boolean,
    val outerHorizontalPaddingDp: Float,
    val outerVerticalPaddingDp: Float,
    val panelMaxWidthDp: Float,
    val panelHorizontalPaddingDp: Float,
    val panelVerticalPaddingDp: Float,
    val panelCornerRadiusDp: Float,
    val hudMaxWidthDp: Float,
    val mascotSizeDp: Float,
    val contentSpacingDp: Float,
    val actionMaxWidthDp: Float,
    val titleMaxWidthDp: Float
)

fun calculateResponsiveLayout(
    widthDp: Float,
    heightDp: Float,
    fontScale: Float = 1f
): ResponsiveLayoutSpec {
    val safeWidth = widthDp.coerceAtLeast(1f)
    val safeHeight = heightDp.coerceAtLeast(1f)
    val safeFontScale = fontScale.coerceIn(0.5f, 3f)
    val effectiveHeight = safeHeight / safeFontScale.coerceAtLeast(1f)
    val isLandscape = safeWidth > safeHeight
    val compactHeight = safeHeight < 480f || effectiveHeight < 390f
    val isMediumWidth = safeWidth >= 600f
    val isExpandedWidth = safeWidth >= 840f

    val windowClass = when {
        isExpandedWidth && !compactHeight -> ResponsiveWindowClass.Expanded
        isLandscape && compactHeight -> ResponsiveWindowClass.CompactLandscape
        isLandscape && isMediumWidth -> ResponsiveWindowClass.MediumLandscape
        !isLandscape && isMediumWidth -> ResponsiveWindowClass.MediumPortrait
        else -> ResponsiveWindowClass.CompactPortrait
    }

    val useHorizontalOverlay = isLandscape && safeWidth >= 560f
    val useCompactHud = compactHeight || safeWidth < 360f
    val stackScoreCards = !useHorizontalOverlay &&
        (safeWidth < 360f || (safeFontScale >= 1.7f && safeWidth < 540f))
    val stackControlHints = !useHorizontalOverlay &&
        (safeWidth < 390f || safeFontScale >= 1.45f)

    val outerHorizontalPadding = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 10f
        ResponsiveWindowClass.CompactPortrait -> 14f
        ResponsiveWindowClass.MediumPortrait,
        ResponsiveWindowClass.MediumLandscape -> 24f
        ResponsiveWindowClass.Expanded -> 32f
    }

    val outerVerticalPadding = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 8f
        ResponsiveWindowClass.CompactPortrait -> 14f
        ResponsiveWindowClass.MediumPortrait,
        ResponsiveWindowClass.MediumLandscape -> 20f
        ResponsiveWindowClass.Expanded -> 28f
    }

    val panelMaxWidth = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 780f
        ResponsiveWindowClass.CompactPortrait -> 456f
        ResponsiveWindowClass.MediumPortrait -> 580f
        ResponsiveWindowClass.MediumLandscape -> 800f
        ResponsiveWindowClass.Expanded -> 880f
    }

    val panelHorizontalPadding = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 16f
        ResponsiveWindowClass.CompactPortrait -> 20f
        ResponsiveWindowClass.MediumPortrait,
        ResponsiveWindowClass.MediumLandscape -> 28f
        ResponsiveWindowClass.Expanded -> 34f
    }

    val panelVerticalPadding = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 12f
        ResponsiveWindowClass.CompactPortrait -> 22f
        ResponsiveWindowClass.MediumPortrait,
        ResponsiveWindowClass.MediumLandscape -> 28f
        ResponsiveWindowClass.Expanded -> 32f
    }

    val hudMaxWidth = when {
        useCompactHud -> 430f
        isExpandedWidth -> 560f
        isMediumWidth -> 520f
        else -> 440f
    }

    val mascotSize = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 76f
        ResponsiveWindowClass.CompactPortrait -> if (safeHeight < 640f) 82f else 98f
        ResponsiveWindowClass.MediumPortrait -> 118f
        ResponsiveWindowClass.MediumLandscape -> 104f
        ResponsiveWindowClass.Expanded -> 126f
    }

    val contentSpacing = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 12f
        ResponsiveWindowClass.CompactPortrait -> 18f
        ResponsiveWindowClass.MediumPortrait,
        ResponsiveWindowClass.MediumLandscape -> 22f
        ResponsiveWindowClass.Expanded -> 26f
    }

    val actionMaxWidth = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 300f
        ResponsiveWindowClass.CompactPortrait -> 420f
        ResponsiveWindowClass.MediumPortrait -> 440f
        ResponsiveWindowClass.MediumLandscape -> 340f
        ResponsiveWindowClass.Expanded -> 380f
    }

    val titleMaxWidth = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 390f
        ResponsiveWindowClass.CompactPortrait -> 420f
        ResponsiveWindowClass.MediumPortrait -> 500f
        ResponsiveWindowClass.MediumLandscape -> 430f
        ResponsiveWindowClass.Expanded -> 500f
    }

    val panelCornerRadius = when (windowClass) {
        ResponsiveWindowClass.CompactLandscape -> 22f
        ResponsiveWindowClass.CompactPortrait -> 28f
        ResponsiveWindowClass.MediumPortrait,
        ResponsiveWindowClass.MediumLandscape -> 32f
        ResponsiveWindowClass.Expanded -> 36f
    }

    return ResponsiveLayoutSpec(
        windowClass = windowClass,
        useHorizontalOverlay = useHorizontalOverlay,
        useCompactHud = useCompactHud,
        stackScoreCards = stackScoreCards,
        stackControlHints = stackControlHints,
        compactHeight = compactHeight,
        outerHorizontalPaddingDp = outerHorizontalPadding,
        outerVerticalPaddingDp = outerVerticalPadding,
        panelMaxWidthDp = panelMaxWidth,
        panelHorizontalPaddingDp = panelHorizontalPadding,
        panelVerticalPaddingDp = panelVerticalPadding,
        panelCornerRadiusDp = panelCornerRadius,
        hudMaxWidthDp = hudMaxWidth,
        mascotSizeDp = mascotSize,
        contentSpacingDp = contentSpacing,
        actionMaxWidthDp = actionMaxWidth,
        titleMaxWidthDp = titleMaxWidth
    )
}
