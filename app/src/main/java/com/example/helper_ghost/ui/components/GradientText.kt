package com.example.helper_ghost.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun GradientText(
    text: String,
    colorsList: List<Color>,
    fontStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = fontStyle.copy(
            brush = Brush.linearGradient(
                colors = colorsList
            )
        ),
        modifier = modifier
    )
}
