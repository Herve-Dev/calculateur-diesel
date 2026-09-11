package com.example.dieselcalculateur.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WearColorPalette = Colors(
    primary = TealPrimary,
    primaryVariant = DarkTeal,
    onPrimary = DarkBackground,
    secondary = AmberSecondary,
    secondaryVariant = AmberSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = OffWhite,
    surface = DarkSurface,
    onSurface = OffWhite,
    onSurfaceVariant = OffWhite.copy(alpha = 0.7f),
    error = Color(0xFFFF5252),
    onError = DarkBackground
)

@Composable
fun DieselCalculateurWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorPalette,
        content = content
    )
}
