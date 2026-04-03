package com.outlier.samplespace.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = NeonBlue300,
    onPrimary = Ink950,
    primaryContainer = Ink700,
    onPrimaryContainer = NeonBlue100,
    secondary = Teal400,
    onSecondary = Ink950,
    secondaryContainer = Color(0xFF11413A),
    onSecondaryContainer = Teal200,
    tertiary = Gold300,
    onTertiary = Ink950,
    tertiaryContainer = Color(0xFF4D3514),
    onTertiaryContainer = Gold300,
    surface = Ink950,
    onSurface = NeonBlue100,
    surfaceContainerLowest = Color(0xFF0C1227),
    surfaceContainerLow = Ink900,
    surfaceContainer = Ink800,
    surfaceContainerHigh = Ink700,
    onSurfaceVariant = Slate200,
    outline = Slate500,
    error = Crimson300,
    onError = Ink950,
    errorContainer = Color(0xFF5B1F2A),
    onErrorContainer = Color(0xFFFFD9DE)
)

private val LightColors = lightColorScheme(
    primary = NeonBlue500,
    onPrimary = Color.White,
    primaryContainer = NeonBlue100,
    onPrimaryContainer = Ink800,
    secondary = Teal400,
    onSecondary = Ink950,
    secondaryContainer = Color(0xFFC7F9F0),
    onSecondaryContainer = Color(0xFF05362F),
    tertiary = Gold500,
    onTertiary = Ink950,
    tertiaryContainer = Color(0xFFFFEDC8),
    onTertiaryContainer = Color(0xFF4E3600),
    surface = Color(0xFFF4F6FF),
    onSurface = Ink900,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEEF2FF),
    surfaceContainer = Color(0xFFE3E9FF),
    surfaceContainerHigh = Color(0xFFD4DEFF),
    onSurfaceVariant = Color(0xFF3B4A7B),
    outline = Color(0xFF7A88B3),
    error = Crimson500,
    onError = Color.White,
    errorContainer = Color(0xFFFFDADF),
    onErrorContainer = Color(0xFF41000D)
)

@Composable
fun OutlierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
