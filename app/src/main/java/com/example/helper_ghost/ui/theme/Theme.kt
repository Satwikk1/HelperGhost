package com.example.helper_ghost.ui.theme

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
    primary = Dark.Primary,
    onPrimary = Dark.PrimaryForeground,
    primaryContainer = GhostCyan,
    onPrimaryContainer = Color.Black,
    inversePrimary = Primary,
    secondary = Dark.Secondary,
    onSecondary = Dark.Foreground,
    secondaryContainer = SlateDark,
    onSecondaryContainer = Color.White,
    tertiary = Chart3,
    onTertiary = Color.White,
    tertiaryContainer = Dark.Muted,
    onTertiaryContainer = Dark.MutedForeground,
    background = Dark.Background,
    onBackground = Dark.Foreground,
    surface = Dark.Card,
    onSurface = Dark.CardForeground,
    surfaceVariant = Dark.Muted,
    onSurfaceVariant = Dark.MutedForeground,
    surfaceTint = Dark.Primary,
    inverseSurface = Foreground,
    inverseOnSurface = Background,
    error = Dark.Destructive,
    onError = Color.White,
    errorContainer = Chart1,
    onErrorContainer = Color.White,
    outline = Dark.Border,
    outlineVariant = Dark.MutedForeground,
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = PrimaryForeground,
    primaryContainer = GhostCyan,
    onPrimaryContainer = TextMain,
    inversePrimary = Dark.Primary,
    secondary = Secondary,
    onSecondary = SecondaryForeground,
    secondaryContainer = SoftGray,
    onSecondaryContainer = TextMain,
    tertiary = Accent,
    onTertiary = AccentForeground,
    tertiaryContainer = InputBackground,
    onTertiaryContainer = MutedForeground,
    background = Background,
    onBackground = Foreground,
    surface = Card,
    onSurface = CardForeground,
    surfaceVariant = Muted,
    onSurfaceVariant = MutedForeground,
    surfaceTint = Primary,
    inverseSurface = Dark.Background,
    inverseOnSurface = Dark.Foreground,
    error = Destructive,
    onError = DestructiveForeground,
    errorContainer = Chart1,
    onErrorContainer = Color.White,
    outline = Border,
    outlineVariant = SwitchBackground,
    scrim = Color.Black,
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