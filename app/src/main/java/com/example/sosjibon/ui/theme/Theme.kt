package com.example.sosjibon.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PrimaryGreen = Color(0xFF159A6C)
private val LightGreenBg = Color(0xFFE8F7F1)
private val DarkForestText = Color(0xFF17332A)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF52B788),
    onPrimary = Color(0xFF0F261E),
    primaryContainer = Color(0xFF1E3A33),
    onPrimaryContainer = Color(0xFFE8F7F1),
    secondary = Color(0xFF40916C),
    onSecondary = Color.White,
    background = Color(0xFF121E1A),
    onBackground = Color(0xFFE8F7F1),
    surface = Color(0xFF1C2C27),
    onSurface = Color(0xFFE8F7F1),
    surfaceVariant = Color(0xFF253B34),
    onSurfaceVariant = Color(0xFFB0C4BE)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreenBg,
    onPrimaryContainer = PrimaryGreen,
    secondary = Color(0xFF2D6A4F),
    onSecondary = Color.White,
    background = Color(0xFFF8FCFA),
    onBackground = DarkForestText,
    surface = Color.White,
    onSurface = DarkForestText,
    surfaceVariant = Color(0xFFF0F7F4),
    onSurfaceVariant = Color(0xFF6B7C75)
)

@Composable
fun SOSJIBONTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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
        typography = Typography,
        content = content
    )
}
