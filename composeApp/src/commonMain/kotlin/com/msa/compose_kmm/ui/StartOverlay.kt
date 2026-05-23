package com.msa.compose_kmm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.utils.BhomaFontFamily

/**
 * صفحه شروع بازی.
 */
@Composable
fun StartOverlay(
    onStartClick: () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GameOverlayBlack)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "زنبور زرنگ",
                color = GameTextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                fontFamily = BhomaFontFamily()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "برای عبور از بین لوله‌ها ضربه بزن",
                color = GameTextWhite,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontFamily = BhomaFontFamily()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                modifier = Modifier.height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamePrimaryOrange
                ),
                onClick = onStartClick
            ) {
                Text(
                    text = "شروع بازی",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontFamily = BhomaFontFamily(),
                    color = GameTextWhite
                )
            }
        }
    }
}