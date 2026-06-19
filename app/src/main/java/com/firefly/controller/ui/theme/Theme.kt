package com.firefly.controller.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 萤火主题色
val FireflyOrange = Color(0xFFFF6B35)
val FireflyYellow = Color(0xFFFFD23F)
val FireflyGreen = Color(0xFF4CAF50)
val FireflyRed = Color(0xFFE53935)
val FireflyBlue = Color(0xFF2196F3)

private val DarkColorScheme = darkColorScheme(
    primary = FireflyOrange,
    secondary = FireflyYellow,
    tertiary = FireflyGreen,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2D2D2D),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FireflyOrange,
    secondary = FireflyYellow,
    tertiary = FireflyGreen,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    surfaceVariant = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1C1C1C),
    onSurface = Color(0xFF1C1C1C)
)

@Composable
fun FireflyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
