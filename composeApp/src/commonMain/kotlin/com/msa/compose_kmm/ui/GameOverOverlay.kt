package com.msa.compose_kmm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
 * Overlay پایان بازی.
 */
@Composable
fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    onRestartClick: () -> Unit
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
                    text = "بازی تمام شد!",
                    color = GameTextWhite,
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BhomaFontFamily(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreCard(
                        modifier = Modifier.weight(1f),
                        label = "امتیاز",
                        value = score.toString()
                    )

                    ScoreCard(
                        modifier = Modifier.weight(1f),
                        label = "رکورد",
                        value = bestScore.toString()
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameLeafGreen,
                        contentColor = GameTextWhite
                    ),
                    onClick = onRestartClick
                ) {
                    Text(
                        text = "شروع دوباره",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BhomaFontFamily()
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .background(GameCardDark, RoundedCornerShape(22.dp))
            .border(
                border = BorderStroke(1.dp, GamePanelBorder),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = GameTextMuted,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontFamily = NazaninFontFamily()
        )

        Text(
            text = value,
            color = GameHoneyYellow,
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            fontFamily = BhomaFontFamily()
        )
    }
}