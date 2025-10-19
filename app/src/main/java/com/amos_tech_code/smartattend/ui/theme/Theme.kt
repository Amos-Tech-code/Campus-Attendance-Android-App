package com.amos_tech_code.smartattend.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary40,
    onPrimary = Neutral100,
    primaryContainer = Primary30,
    onPrimaryContainer = Primary90,
    inversePrimary = Primary80,

    secondary = Secondary40,
    onSecondary = Neutral100,
    secondaryContainer = Secondary30,
    onSecondaryContainer = Secondary90,

    tertiary = Tertiary40,
    onTertiary = Neutral100,
    tertiaryContainer = Tertiary30,
    onTertiaryContainer = Tertiary90,

    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral20,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,

    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,

    error = Error40,
    onError = Neutral100,
    errorContainer = Error30,
    onErrorContainer = Error90,

    scrim = Neutral0.copy(alpha = 0.5f),
    surfaceTint = Primary40
)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = Neutral100,
    primaryContainer = Primary90,
    onPrimaryContainer = Primary10,
    inversePrimary = Primary80,

    secondary = Secondary40,
    onSecondary = Neutral100,
    secondaryContainer = Secondary90,
    onSecondaryContainer = Secondary10,

    tertiary = Tertiary40,
    onTertiary = Neutral100,
    tertiaryContainer = Tertiary90,
    onTertiaryContainer = Tertiary10,

    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral95,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,

    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,

    error = Error40,
    onError = Neutral100,
    errorContainer = Error90,
    onErrorContainer = Error10,

    scrim = Neutral0.copy(alpha = 0.5f),
    surfaceTint = Primary40
)

@Composable
fun SmartAttendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    // Set system bar colors
    val view = LocalView.current
    val context = LocalContext.current
    if (!view.isInEditMode) {
        SideEffect {
            (context as? Activity)?.window?.let { window ->
                // Set status bar color
                window.statusBarColor = colorScheme.surface.toArgb()

                // Set navigation bar color
                window.navigationBarColor = colorScheme.primaryContainer.toArgb()

                val insetsController = ViewCompat.getWindowInsetsController(view)

                // Control light/dark appearance for status & nav bars
                insetsController?.isAppearanceLightStatusBars = !darkTheme
                insetsController?.isAppearanceLightNavigationBars = !darkTheme
            }
        }

    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}