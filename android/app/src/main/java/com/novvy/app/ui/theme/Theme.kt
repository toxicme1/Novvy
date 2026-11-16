package com.novvy.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DEFF),
    onPrimaryContainer = Color(0xFF1B0B57),
    secondary = Cyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFFC7F5FC),
    onSecondaryContainer = Color(0xFF002F34),
    tertiary = Coral,
    onTertiary = Color(0xFF3E1500),
    tertiaryContainer = Color(0xFFFFDBCB),
    onTertiaryContainer = Color(0xFF2A1000),
    background = LightBg,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = ErrorRed,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9A8FF),
    onPrimary = Color(0xFF2A1670),
    primaryContainer = Color(0xFF4A31C4),
    onPrimaryContainer = Color(0xFFE7E0FF),
    secondary = Cyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF0B4A52),
    onSecondaryContainer = Color(0xFFC7F5FC),
    tertiary = Coral,
    onTertiary = Color(0xFF3E1500),
    tertiaryContainer = Color(0xFF6B2A00),
    onTertiaryContainer = Color(0xFFFFDBCB),
    background = DarkBg,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005),
)

@Composable
fun NovvyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = NovvyTypography,
        shapes = NovvyShapes,
        content = content,
    )
}