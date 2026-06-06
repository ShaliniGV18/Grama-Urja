package com.gramaurja.presentation.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FolkOrange,
    onPrimary = OnPrimaryWhite,
    primaryContainer = FolkBeige,
    onPrimaryContainer = FolkBrown,
    secondary = PowerBlueMedium,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = PowerBlueSurface,
    onSecondaryContainer = PowerBlueDark,
    tertiary = AmberWarning,
    onTertiary = OnPrimaryWhite,
    error = PowerRedMedium,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = FolkBeige,
    onSurfaceVariant = UnknownGrey,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = FolkAmber,
    onPrimary = BackgroundDark,
    primaryContainer = FolkBrown,
    onPrimaryContainer = FolkBeige,
    secondary = PowerBlueLight,
    onSecondary = BackgroundDark,
    secondaryContainer = PowerBlueDark,
    onSecondaryContainer = OnSurfaceDark,
    tertiary = AmberLight,
    onTertiary = BackgroundDark,
    error = PowerRedLight,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark,
    outline = OutlineDark
)

@Composable
fun GramaUrjaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = findActivity(view.context)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GramaUrjaTypography,
        content = content
    )
}

private fun findActivity(context: Context): Activity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}
