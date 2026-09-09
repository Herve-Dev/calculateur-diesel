package com.example.dieselcalculateur.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dieselcalculateur.ui.theme.AmberSecondary
import com.example.dieselcalculateur.ui.theme.DarkBackground
import com.example.dieselcalculateur.ui.theme.DarkSurface
import com.example.dieselcalculateur.ui.theme.OffWhite
import com.example.dieselcalculateur.ui.theme.TealPrimary

enum class PillButtonVariant {
    PRIMARY,   // Fond ambre plein, texte sombre
    SECONDARY, // Fond neutre sombre, texte clair
    TEAL       // Fond teal plein, texte sombre
}

/**
 * Bouton au format pilule (forme capsule très arrondie).
 * Propose plusieurs variantes (Primaire ambre, Secondaire sombre, Teal) avec un effet de bordure lumineuse.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillButtonVariant = PillButtonVariant.PRIMARY,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val backgroundColor = when (variant) {
        PillButtonVariant.PRIMARY -> if (enabled) AmberSecondary else AmberSecondary.copy(alpha = 0.4f)
        PillButtonVariant.SECONDARY -> if (enabled) DarkSurface else DarkSurface.copy(alpha = 0.5f)
        PillButtonVariant.TEAL -> if (enabled) TealPrimary else TealPrimary.copy(alpha = 0.4f)
    }

    val contentColor = when (variant) {
        PillButtonVariant.PRIMARY -> DarkBackground
        PillButtonVariant.SECONDARY -> if (enabled) OffWhite else OffWhite.copy(alpha = 0.4f)
        PillButtonVariant.TEAL -> DarkBackground
    }

    val glowColor = when (variant) {
        PillButtonVariant.PRIMARY -> if (enabled) AmberSecondary else Color.Transparent
        PillButtonVariant.SECONDARY -> Color(0xFF2E3A46)
        PillButtonVariant.TEAL -> if (enabled) TealPrimary else Color.Transparent
    }

    val shape = CircleShape

    Box(
        modifier = modifier
            .glowBorder(
                color = glowColor,
                shape = shape,
                borderWidth = 1.dp,
                glowRadius = if (variant == PillButtonVariant.SECONDARY) 0.dp else 6.dp,
                glowAlpha = 0.35f
            )
            .background(backgroundColor, shape = shape)
            .clip(shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.wrapContentSize()
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Bouton de choix/sélection au format pilule (ex: liste de carburants B7, E10, E85, SP95).
 * Bascule dynamiquement entre un état sélectionné (fond ambre + glow) et un état non sélectionné.
 */
@Composable
fun PillSelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = AmberSecondary
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (selected) selectedColor else DarkSurface,
        animationSpec = tween(200),
        label = "PillSelectableBg"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) DarkBackground else OffWhite,
        animationSpec = tween(200),
        label = "PillSelectableText"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) selectedColor else Color(0xFF2E3A46),
        animationSpec = tween(200),
        label = "PillSelectableBorder"
    )

    val shape = CircleShape

    Box(
        modifier = modifier
            .glowBorder(
                color = animatedBorderColor,
                shape = shape,
                borderWidth = 1.dp,
                glowRadius = if (selected) 6.dp else 0.dp,
                glowAlpha = if (selected) 0.4f else 0f
            )
            .background(animatedBgColor, shape = shape)
            .clip(shape)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = animatedTextColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
