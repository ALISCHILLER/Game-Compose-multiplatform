package com.msa.compose_kmm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.utils.BhomaFontFamily
import com.msa.compose_kmm.utils.NazaninFontFamily

/**
 * HUD بالای صفحه.
 */
@Composable
fun GameHud(
    score: Int,
    bestScore: Int
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HudChip(label = "امتیاز", value = score.toString())
            HudChip(label = "رکورد", value = bestScore.toString())
        }
    }
}

@Composable
private fun HudChip(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .background(
                color = GameCardDark,
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                border = BorderStroke(1.dp, GamePanelBorder),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 18.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            color = GameTextMuted,
            fontSize = MaterialTheme.typography.labelLarge.fontSize,
            fontFamily = NazaninFontFamily()
        )

        Text(
            text = value,
            color = GameHoneyYellow,
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontFamily = BhomaFontFamily()
        )
    }
}