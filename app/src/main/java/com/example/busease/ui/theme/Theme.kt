package com.example.busease.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Vibrant transit teal & emerald palette
val PrimaryTeal = Color(0xFF0D9488)
val PrimaryTealDark = Color(0xFF14B8A6)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFCCFBF1)
val OnPrimaryContainer = Color(0xFF115E59)

val SecondaryAmber = Color(0xFFD97706)
val SecondaryContainer = Color(0xFFFEF3C7)
val OnSecondaryContainer = Color(0xFF92400E)

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF1E293B)
val OutlineLight = Color(0xFFCBD5E1)

val BackgroundDark = Color(0xFF0B1319)
val SurfaceDark = Color(0xFF131F27)
val SurfaceVariantDark = Color(0xFF1E2E38)
val OnBackgroundDark = Color(0xFFF1F5F9)
val OnSurfaceDark = Color(0xFFE2E8F0)
val OutlineDark = Color(0xFF334155)

val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryAmber,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    outline = OutlineLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryTealDark,
    onPrimary = Color(0xFF003732),
    primaryContainer = Color(0xFF005049),
    onPrimaryContainer = Color(0xFF70F7E7),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = Color(0xFFFFDDB3),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    outline = OutlineDark
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
