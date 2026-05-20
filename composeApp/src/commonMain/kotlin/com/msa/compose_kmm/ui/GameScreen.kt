package com.msa.compose_kmm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource

/**
 * پس‌زمینه ثابت بازی.
 *
 * این نسخه فقط از background.png استفاده می‌کند.
 */
@Composable
fun GameBackground() {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(Res.drawable.background),
        contentDescription = "تصویر پس‌زمینه بازی",
        contentScale = ContentScale.Crop
    )
}