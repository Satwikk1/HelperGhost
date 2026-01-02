package com.example.helper_ghost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GhostFloatingBubble(onTriggerScreenshot: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color(0xFF00F5FF))
            .clickable() { onTriggerScreenshot() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.BubbleChart,
            contentDescription = "Ghost AI",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}