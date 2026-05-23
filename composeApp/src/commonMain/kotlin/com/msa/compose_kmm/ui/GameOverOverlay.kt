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
 * صفحه پایان بازی.
 */
@Composable
fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    onRestartClick: () -> Unit
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
                text = "بازی تمام شد!",
                color = GameTextWhite,
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = BhomaFontFamily()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "امتیاز تو: $score",
                color = GameTextWhite,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontFamily = BhomaFontFamily()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "رکورد: $bestScore",
                color = GameHoneyYellow,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = BhomaFontFamily()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                modifier = Modifier.height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamePrimaryOrange
                ),
                onClick = onRestartClick
            ) {
                Text(
                    text = "شروع دوباره",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontFamily = BhomaFontFamily(),
                    color = GameTextWhite
                )
            }
        }
    }
}