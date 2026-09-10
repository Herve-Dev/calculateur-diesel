package com.example.dieselcalculateur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dieselcalculateur.ui.theme.AmberSecondary
import com.example.dieselcalculateur.ui.theme.DarkSurface
import com.example.dieselcalculateur.ui.theme.TealPrimary

enum class GlowCardType {
    TEAL,
    AMBER,
    NEUTRAL
}

/**
 * Card à coins très arrondis dotée d'une bordure lumineuse configurable.
 * Idéale pour les cartes de résultats (Teal/Amber), les cartes de stations ou d'historique.
 */
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    type: GlowCardType = GlowCardType.NEUTRAL,
    customGlowColor: Color? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.5.dp,
    glowRadius: Dp = 8.dp,
    glowAlpha: Float = 0.35f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val borderGlowColor = customGlowColor ?: when (type) {
        GlowCardType.TEAL -> TealPrimary
        GlowCardType.AMBER -> AmberSecondary
        GlowCardType.NEUTRAL -> Color(0xFF2E3A46)
    }

    val isGlowing = type != GlowCardType.NEUTRAL || customGlowColor != null
    val effectiveGlowRadius = if (isGlowing) glowRadius else 0.dp
    val effectiveGlowAlpha = if (isGlowing) glowAlpha else 0f

    // Marge de sécurité externe pour contenir le dessin du glow sans débordement hors écran
    val glowMargin = if (isGlowing) (effectiveGlowRadius / 2) else 2.dp

    Box(
        modifier = modifier
            .padding(glowMargin)
            .glowBorder(
                color = borderGlowColor,
                shape = shape,
                borderWidth = borderWidth,
                glowRadius = effectiveGlowRadius,
                glowAlpha = effectiveGlowAlpha
            )
            .background(DarkSurface, shape = shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        content = content
    )
}
