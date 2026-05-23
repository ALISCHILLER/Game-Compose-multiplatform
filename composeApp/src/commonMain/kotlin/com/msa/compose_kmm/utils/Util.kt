package com.msa.compose_kmm.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.bhoma
import compose_kmm.composeapp.generated.resources.nazanin
import org.jetbrains.compose.resources.Font

/**
 * فونت فارسی Nazanin برای متن‌های توضیحی.
 */
@Composable
fun NazaninFontFamily(): FontFamily {
    val font = Font(Res.font.nazanin)
    return remember(font) { FontFamily(font) }
}

/**
 * فونت فارسی B Homa برای تیترها و عددهای مهم.
 */
@Composable
fun BhomaFontFamily(): FontFamily {
    val font = Font(Res.font.bhoma)
    return remember(font) { FontFamily(font) }
}