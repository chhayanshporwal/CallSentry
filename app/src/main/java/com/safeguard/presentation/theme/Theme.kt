package com.safeguard.presentation.theme

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

private val LightColorScheme =
        lightColorScheme(
                primary = Primary,
                onPrimary = Color.White,
                primaryContainer = PrimaryLight,
                onPrimaryContainer = PrimaryDark,
                secondary = Secondary,
                onSecondary = Color.White,
                secondaryContainer = SecondaryLight,
                onSecondaryContainer = SecondaryDark,
                tertiary = Accent,
                onTertiary = Color.White,
                error = Error,
                onError = Color.White,
                background = LightBackground,
                onBackground = LightOnBackground,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                outline = LightOnSurfaceVariant
        )

private val DarkColorScheme =
        darkColorScheme(
                primary = PrimaryLight,
                onPrimary = PrimaryDark,
                primaryContainer = Primary,
                onPrimaryContainer = Color.White,
                secondary = SecondaryLight,
                onSecondary = SecondaryDark,
                secondaryContainer = Secondary,
                onSecondaryContainer = Color.White,
                tertiary = Accent,
                onTertiary = Color.White,
                error = Error,
                onError = Color.White,
                background = DarkBackground,
                onBackground = DarkOnBackground,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                outline = DarkOnSurfaceVariant
        )

@Composable
fun SafeGuardTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
        val colorScheme =
                when {
                        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                                val context = LocalContext.current
                                if (darkTheme) dynamicDarkColorScheme(context)
                                else dynamicLightColorScheme(context)
                        }
                        darkTheme -> DarkColorScheme
                        else -> LightColorScheme
                }

        val view = LocalView.current
        if (!view.isInEditMode) {
                SideEffect {
                        val window = (view.context as Activity).window
                        @Suppress("DEPRECATION")
                        window.statusBarColor = colorScheme.background.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                                !darkTheme
                }
        }

        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
