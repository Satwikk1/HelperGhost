package com.example.helper_ghost.ui.theme

import androidx.compose.ui.graphics.Color

val GhostCyan = Color(0xFF00F5FF)
val SlateDark = Color(0xFF1E293B)
val TextMain = Color(0xFF0F172A)
val SoftGray = Color(0xFFE5E5E5)

val Background = Color(0xFFFFFFFF)
val Foreground = Color(0xFF252525)
val Card = Color(0xFFFFFFFF)
val CardForeground = Color(0xFF252525)
val Primary = Color(0xFF030213)
val PrimaryForeground = Color(0xFFFFFFFF)
val Secondary = Color(0xFFF1F1F1)
val SecondaryForeground = Color(0xFF030213)
val Muted = Color(0xFFECECF0)
val MutedForeground = Color(0xFF717182)
val Accent = Color(0xFFE9EBEF)
val AccentForeground = Color(0xFF030213)
val Destructive = Color(0xFFD4183D)
val DestructiveForeground = Color(0xFFFFFFFF)
val Border = Color(0x1A000000)
val InputBackground = Color(0xFFF3F3F5)
val SwitchBackground = Color(0xFFCBCED4)

object Dark {
    val Background = Color(0xFF252525)
    val Foreground = Color(0xFFFAFAFA)
    val Card = Color(0xFF252525)
    val CardForeground = Color(0xFFFAFAFA)
    val Primary = Color(0xFFFAFAFA)
    val PrimaryForeground = Color(0xFF353535)
    val Secondary = Color(0xFF414141)
    val Muted = Color(0xFF414141)
    val MutedForeground = Color(0xFFB1B1B1)
    val Border = Color(0xFF414141)
    val Destructive = Color(0xFF6B1A1A)
}

val Chart1 = Color(0xFFE54D2E)
val Chart2 = Color(0xFF3EAF7C)
val Chart3 = Color(0xFF007ACC)
val Chart4 = Color(0xFFE7C000)
val Chart5 = Color(0xFFD04D00)

object AppColors {
    object Purple {
        val deep = Color(0xFF9333EA)
        val medium = Color(0xFFA855F7)
        val light = Color(0xFFC084FC)
        val lighter = Color(0xFFC4B5FD)
        val lightest = Color(0xFFDDD6FE)
        val tint = Color(0xFFF3E8FF)
        val background = Color(0xFFFAF5FF)
    }

    object Pink {
        val hot = Color(0xFFEC4899)
        val light = Color(0xFFF472B6)
        val lighter = Color(0xFFFBCFE8)
        val background = Color(0xFFFCE7F3)
        val tint = Color(0xFFFDF2F8)
    }

    object Orange {
        val vibrant = Color(0xFFF97316)
        val medium = Color(0xFFFB923C)
        val light = Color(0xFFFDBA74)
        val lighter = Color(0xFFFED7AA)
        val background = Color(0xFFFFF7ED)
    }

    object Blue {
        val primary = Color(0xFF3B82F6)
        val light = Color(0xFF93C5FD)
        val lighter = Color(0xFFBFDBFE)
        val background = Color(0xFFDBEAFE)
        val tint = Color(0xFFEFF6FF)
    }

    object Yellow {
        val vibrant = Color(0xFFEAB308)
        val gold = Color(0xFFFBBF24)
        val medium = Color(0xFFFCD34D)
        val light = Color(0xFFFEF08A)
        val background = Color(0xFFFEF3C7)
    }

    object Emerald {
        val light = Color(0xFFA7F3D0)
        val medium = Color(0xFF6EE7B7)
        val vibrant = Color(0xFF34D399)
    }

    val white = Color(0xFFFFFFFF)
}

object SemanticColors {
    val brandPrimary = AppColors.Purple.deep
    val brandAccent = AppColors.Pink.hot
    val brandSecondary = AppColors.Orange.vibrant
    val active = AppColors.Emerald.vibrant
    val inactive = AppColors.Purple.light
    val hover = AppColors.Purple.background
    val focus = AppColors.Purple.medium
    val pageBackground = AppColors.Blue.background
    val cardBackground = AppColors.white
    val overlayBackground = AppColors.Purple.background
}

object AppGradients {
    val title = listOf(AppColors.Purple.deep, AppColors.Pink.hot, AppColors.Orange.vibrant)
    val pageBackground = listOf(AppColors.Blue.background, AppColors.Purple.background, AppColors.Yellow.background)
    val serviceCardBackground = listOf(AppColors.Purple.light, AppColors.Pink.light, AppColors.Orange.medium)
    val serviceCardOverlay = listOf(AppColors.white, AppColors.white)
    val activePersonaOverlay = listOf(AppColors.Purple.background, AppColors.Pink.background, AppColors.Orange.background)
    val activePersonaIndicator = listOf(AppColors.Purple.medium, AppColors.Pink.hot)
    val activePersonaIconBackground = listOf(AppColors.Purple.tint, AppColors.Pink.background)
    val iconExecutive = listOf(AppColors.Purple.deep)
    val iconRomantic = listOf(AppColors.Pink.hot)
    val iconWitty = listOf(AppColors.Orange.vibrant)
    val statInteractionsNumber = listOf(AppColors.Blue.primary, AppColors.Purple.medium)
    val statActiveChatsNumber = listOf(AppColors.Purple.medium, AppColors.Pink.hot)
    val statSuccessRateNumber = listOf(AppColors.Orange.vibrant, AppColors.Yellow.vibrant)
    val statInteractionsHover = listOf(AppColors.Blue.tint)
    val statActiveChatsHover = listOf(AppColors.Pink.tint)
    val statSuccessRateHover = listOf(AppColors.Orange.background)
    val orbBlue = listOf(AppColors.Blue.light, AppColors.Blue.lighter)
    val orbPinkOrange = listOf(AppColors.Pink.lighter, AppColors.Orange.lighter, AppColors.Orange.light)
    val orbYellow = listOf(AppColors.Yellow.light, AppColors.Yellow.medium)
    val orbPurple = listOf(AppColors.Purple.lighter, AppColors.Purple.lightest)
    val orbGold = listOf(AppColors.Yellow.gold, AppColors.Yellow.medium)
    val emerald = listOf(AppColors.Emerald.light, AppColors.Emerald.medium, AppColors.Emerald.vibrant)
}
