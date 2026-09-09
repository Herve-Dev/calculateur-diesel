package com.example.dieselcalculateur.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier ajoutant un effet de bordure lumineuse ("Glow") autour d'une forme.
 * Dessine plusieurs contours concentriques avec une opacité dégressive pour un effet néon doux et performant.
 */
fun Modifier.glowBorder(
    color: Color,
    shape: Shape,
    borderWidth: Dp = 1.5.dp,
    glowRadius: Dp = 6.dp,
    glowAlpha: Float = 0.4f,
    enabled: Boolean = true
): Modifier = if (!enabled || color == Color.Unspecified || color == Color.Transparent || (glowAlpha <= 0f && borderWidth <= 0.dp)) {
    this
} else this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)

    // Couches de halo lumineux extérieur (glow)
    if (glowRadius > 0.dp && glowAlpha > 0f) {
        val layers = 4
        val radiusPx = glowRadius.toPx()
        val borderPx = borderWidth.toPx()

        for (i in layers downTo 1) {
            val strokeWidth = borderPx + (radiusPx * (i.toFloat() / layers))
            val alpha = (glowAlpha / layers) * (1f - (i - 1).toFloat() / layers)
            drawOutlineHelper(
                outline = outline,
                color = color.copy(alpha = alpha),
                style = Stroke(width = strokeWidth)
            )
        }
    }

    // Bordure nette intérieure
    if (borderWidth > 0.dp) {
        drawOutlineHelper(
            outline = outline,
            color = color,
            style = Stroke(width = borderWidth.toPx())
        )
    }
}

private fun DrawScope.drawOutlineHelper(
    outline: Outline,
    color: Color,
    style: DrawStyle = Fill
) {
    when (outline) {
        is Outline.Rectangle -> drawRect(
            color = color,
            topLeft = Offset(outline.rect.left, outline.rect.top),
            size = Size(outline.rect.width, outline.rect.height),
            style = style
        )
        is Outline.Rounded -> {
            val rrect = outline.roundRect
            drawRoundRect(
                color = color,
                topLeft = Offset(rrect.left, rrect.top),
                size = Size(rrect.width, rrect.height),
                cornerRadius = CornerRadius(rrect.topLeftCornerRadius.x, rrect.topLeftCornerRadius.y),
                style = style
            )
        }
        is Outline.Generic -> drawPath(
            path = outline.path,
            color = color,
            style = style
        )
    }
}
