package com.example.helper_ghost.ui.screens.landingScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.helper_ghost.ui.theme.AppGradients

@Composable
fun PersonaSection(
    selectedPersonas: Set<String>,
    onPersonaToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(bottom = 32.dp)
    ) {
        PersonaCard(
            title = "Executive",
            subtitle = "Professional and decisive",
            icon = Icons.Default.Person,
            iconGradient = AppGradients.iconExecutive,
            isSelected = selectedPersonas.contains("Executive"),
            onPress = { onPersonaToggle("Executive") }
        )

        PersonaCard(
            title = "Romantic",
            subtitle = "Warm and thoughtful",
            icon = Icons.Default.Favorite,
            iconGradient = AppGradients.iconRomantic,
            isSelected = selectedPersonas.contains("Romantic"),
            onPress = { onPersonaToggle("Romantic") }
        )

        PersonaCard(
            title = "Witty",
            subtitle = "Clever and engaging",
            icon = Icons.Default.ChatBubble,
            iconGradient = AppGradients.iconWitty,
            isSelected = selectedPersonas.contains("Witty"),
            onPress = { onPersonaToggle("Witty") }
        )
    }
}
