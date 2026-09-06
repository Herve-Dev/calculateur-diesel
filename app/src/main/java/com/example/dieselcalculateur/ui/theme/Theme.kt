package com.example.dieselcalculateur.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CustomDarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DarkTeal,
    onPrimaryContainer = OffWhite,
    secondary = AmberSecondary,
    onSecondary = DarkBackground,
    tertiary = AmberSecondary,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = OffWhite,
    surface = DarkSurface,
    onSurface = OffWhite,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = OffWhite
)

@Composable
fun DieselCalculateurTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}
