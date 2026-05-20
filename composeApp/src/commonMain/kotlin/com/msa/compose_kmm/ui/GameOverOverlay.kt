package com.msa.compose_kmm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.utils.BhomaFontFamily

/**
 * لایه پایان بازی.
 */
@Composable
fun GameOverOverlay(
    score: Int,
    onRestartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.55f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "شکست خوردی!",
            color = Color.White,
            fontSize = MaterialTheme.typography.displayMedium.fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = BhomaFontFamily()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "امتیاز: $score",
            color = Color.White,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontFamily = BhomaFontFamily()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(size = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCB5C0C)
            ),
            onClick = onRestartClick
        ) {
            Text(
                text = "شروع دوباره",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontFamily = BhomaFontFamily(),
                color = Color.White
            )
        }
    }
}