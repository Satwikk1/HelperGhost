package com.example.helper_ghost.ui.screens.landingScreen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helper_ghost.ui.theme.AppColors
import com.example.helper_ghost.ui.theme.AppGradients
import com.example.helper_ghost.ui.theme.MutedForeground

@Composable
fun PersonaCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconGradient: List<Color>,
    isSelected: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = iconGradient.first().copy(alpha = 0.5f),
                        spotColor = iconGradient.first()
                    )
                } else Modifier
            )
            .clickable { onPress() },
        shape = RoundedCornerShape(32.dp),
        border = if (isSelected) BorderStroke(1.dp, Brush.linearGradient(AppGradients.activePersonaIndicator)) else null,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) Brush.linearGradient(AppGradients.activePersonaOverlay)
                    else Brush.linearGradient(listOf(Color.White, Color.White))
                )
                .padding(24.dp)
        ) {
            // Blinking Dot
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .graphicsLayer(alpha = alpha)
                        .background(
                            brush = Brush.linearGradient(AppGradients.activePersonaIndicator),
                            shape = CircleShape
                        )
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                           if(isSelected) AppColors.Purple.tint else AppColors.Purple.background
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = iconGradient.first()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Text Content
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = iconGradient.first()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) iconGradient.first().copy(alpha = 0.7f) else MutedForeground
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonaCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        PersonaCard(
            title = "Executive",
            subtitle = "Professional and decisive",
            icon = Icons.Default.Person,
            iconGradient = AppGradients.iconExecutive,
            isSelected = true,
            onPress = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        PersonaCard(
            title = "Romantic",
            subtitle = "Sweet and charming",
            icon = Icons.Default.Person,
            iconGradient = AppGradients.iconRomantic,
            isSelected = false,
            onPress = {}
        )
    }
}
