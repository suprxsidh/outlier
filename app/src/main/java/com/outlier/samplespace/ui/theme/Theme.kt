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
    primary = Blue300,
    onPrimary = ColorNavy,
    primaryContainer = Blue700,
    onPrimaryContainer = Blue100,
    tertiary = Amber300,
    onTertiary = ColorNavy,
    surface = ColorNavy,
    onSurface = Blue50,
    surfaceContainer = Blue900,
    surfaceContainerHigh = Blue800,
    onSurfaceVariant = Blue200,
    error = Red300,
    onError = ColorNavy
)

private val LightColors = lightColorScheme(
    primary = Blue700,
    onPrimary = Blue50,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    tertiary = Amber500,
    onTertiary = Blue900,
    surface = Blue50,
    onSurface = Blue900,
    surfaceContainer = Color(0xFFE8EEFF),
    surfaceContainerLow = Color(0xFFF3F6FF),
    surfaceContainerLowest = Color(0xFFF8FAFF),
    surfaceContainerHigh = Color(0xFFDDE6FF),
    onSurfaceVariant = Color(0xFF3A4A73),
    error = Red500,
    onError = Blue50
)

@Composable
fun OutlierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
