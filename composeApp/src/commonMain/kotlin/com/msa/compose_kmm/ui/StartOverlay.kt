package com.msa.compose_kmm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.msa.compose_kmm.utils.BhomaFontFamily

/**
 * لایه شروع بازی.
 */
@Composable
fun StartOverlay(
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(size = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCB5C0C)
            ),
            onClick = onStartClick
        ) {
            Text(
                text = "شروع بازی",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontFamily = BhomaFontFamily(),
                color = Color.White
            )
        }
    }
}