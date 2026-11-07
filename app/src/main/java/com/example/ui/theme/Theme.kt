package com.example.ui.theme

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
    primary = DiwaliGold,
    onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFF5D2C04),
    onPrimaryContainer = Color(0xFFFFDDB8),
    secondary = DiwaliAmber,
    onSecondary = Color(0xFF3E2723),
    tertiary = DiwaliVermilion,
    onTertiary = Color.White,
    background = DiwaliDeepDark,
    onBackground = DiwaliTextPrimary,
    surface = DiwaliSurfaceDark,
    onSurface = DiwaliTextPrimary,
    surfaceVariant = DiwaliSurfaceElevated,
    onSurfaceVariant = DiwaliTextSecondary,
    outline = DiwaliBorderGold
)

private val LightColorScheme = lightColorScheme(
    primary = DiwaliSaffron,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4E1A00),
    secondary = DiwaliAmber,
    onSecondary = Color.Black,
    tertiary = DiwaliVermilion,
    onTertiary = Color.White,
    background = Color(0xFFFFFBF2),
    onBackground = Color(0xFF2E1707),
    surface = Color(0xFFFFF7E6),
    onSurface = Color(0xFF2E1707),
    surfaceVariant = Color(0xFFFFECCC),
    onSurfaceVariant = Color(0xFF5A3515),
    outline = Color(0xFFD4A373)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to rich festive dark celebration mode
    dynamicColor: Boolean = false, // Keep the authentic warm Diwali palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
