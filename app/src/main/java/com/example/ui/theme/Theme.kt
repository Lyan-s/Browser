package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBg,
    primaryContainer = CyberSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = NeonViolet,
    onSecondary = TextPrimary,
    secondaryContainer = CyberSurfaceElevated,
    onSecondaryContainer = NeonViolet,
    tertiary = NeonMagenta,
    onTertiary = TextPrimary,
    background = CyberBg,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = NeonRed,
    onError = TextPrimary
)

@Composable
fun CyberNetTheme(
    darkTheme: Boolean = true, // Default to cyberpunk dark mode
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CyberBg.toArgb()
                window.navigationBarColor = CyberBg.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = CyberNetTheme(content = content)
