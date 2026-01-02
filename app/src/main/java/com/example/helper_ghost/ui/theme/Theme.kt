package com.example.helper_ghost.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GhostCyan,
    onPrimary = SlateDark,
    secondary = SoftGray,
    onSecondary = Color.White,
    surface = SlateDark,
    background = Color(0xFF020617), // Deepest midnight blue
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFF475569),
)

private val LightColorScheme = lightColorScheme(
    primary = GhostCyan,
    secondary = SlateDark,
    background = BgLight,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TextMain,
    onSurface = TextMain
)

@Composable
fun GhostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = GhostTypography,
        content = content
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GhostTypography,
        content = content
    )
}