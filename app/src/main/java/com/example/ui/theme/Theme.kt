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

// We use the Editorial Aesthetic light palette as the standard for both themes
// to keep a consistent, stunning, publication-grade layout throughout the app.
private val EditorialColorScheme = lightColorScheme(
    primary = EditorialPrimary,
    secondary = TealAccent,
    tertiary = LavenderAccent,
    background = EditorialBackground,
    surface = EditorialSurface,
    surfaceVariant = EditorialSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = EditorialTitle,
    onBackground = EditorialDarkText,
    onSurface = EditorialDarkText,
    onSurfaceVariant = EditorialTextSecondary,
    outline = EditorialBorder,
    error = CrimsonAlert
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    // We enforce the Editorial Aesthetic light/warm palette to preserve the gorgeous styling.
    val colorScheme = EditorialColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
