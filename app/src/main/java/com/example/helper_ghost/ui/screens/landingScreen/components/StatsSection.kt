package com.example.helper_ghost.ui.screens.landingScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helper_ghost.ui.components.GradientText
import com.example.helper_ghost.ui.theme.AppColors
import com.example.helper_ghost.ui.theme.AppGradients

@Composable
fun StatCard(
    label: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GradientText(
                text = value,
                colorsList = gradientColors,
                fontStyle = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Purple.medium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatsSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Interactions",
            value = "247",
            gradientColors = AppGradients.statInteractionsNumber,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Active Chats",
            value = "18",
            gradientColors = AppGradients.statActiveChatsNumber,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Success",
            value = "94%",
            gradientColors = AppGradients.statSuccessRateNumber,
            modifier = Modifier.weight(1f)
        )
    }
}
