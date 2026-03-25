package com.smartgallery.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricTeal,
    secondary = NeonCoral,
    background = Midnight,
    surface = DeepSlate,
    onPrimary = Midnight,
    onSecondary = Midnight,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

@Composable
fun SmartGalleryTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        darkTheme || isSystemInDarkTheme() -> DarkColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SmartTypography,
        shapes = SmartShapes,
        content = content
    )
}
