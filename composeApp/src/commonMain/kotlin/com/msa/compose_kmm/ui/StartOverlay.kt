package com.msa.compose_kmm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.utils.BhomaFontFamily
import com.msa.compose_kmm.utils.NazaninFontFamily

/**
 * Overlay شروع بازی.
 */
@Composable
fun StartOverlay(
    onStartClick: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GameOverlayBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .background(GamePanel, RoundedCornerShape(32.dp))
                    .border(
                        border = BorderStroke(1.dp, GamePanelBorder),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "زنبور زرنگ",
                    color = GameTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontFamily = BhomaFontFamily(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "از شکاف بین لوله‌ها رد شو، رکورد بزن و زمین نخوری!",
                    color = GameTextMuted,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontFamily = NazaninFontFamily(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePrimaryOrange,
                        contentColor = GameTextWhite
                    ),
                    onClick = onStartClick
                ) {
                    Text(
                        text = "شروع بازی",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BhomaFontFamily()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "برای پرش روی صفحه ضربه بزن",
                    color = GameTextMuted,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontFamily = NazaninFontFamily(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}