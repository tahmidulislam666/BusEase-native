package com.example.busease.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Classic Flutter transit palette (Royal Blue / Indigo primary with warm Amber / Tangerine accent)
val PrimaryBlue = Color(0xFF1E3A8A)          // Classic Flutter indigo-blue primary (e.g. Colors.indigo.shade900 / blue[800])
val PrimaryBlueMedium = Color(0xFF2563EB)    // Vibrant Flutter blue 600
val PrimaryBlueLight = Color(0xFF60A5FA)     // Light blue for dark mode
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFDBEAFE)     // Soft blue container
val OnPrimaryContainer = Color(0xFF1E3A8A)

val AccentAmber = Color(0xFFF59E0B)          // Flutter Colors.amber[700] / Colors.orange
val AccentAmberContainer = Color(0xFFFEF3C7)
val OnAccentAmberContainer = Color(0xFF78350F)

val BackgroundLight = Color(0xFFF8FAFC)      // Clean Flutter scaffold background (e.g., Colors.grey[50])
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEFF6FF)  // Hint of soft blue surface tint
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF1E293B)
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)

val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceVariantDark = Color(0xFF334155)
val OnBackgroundDark = Color(0xFFF8FAFC)
val OnSurfaceDark = Color(0xFFF1F5F9)
val OutlineDark = Color(0xFF475569)

val LightColorScheme = lightColorScheme(
    primary = PrimaryBlueMedium,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = AccentAmberContainer,
    onSecondaryContainer = OnAccentAmberContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
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
