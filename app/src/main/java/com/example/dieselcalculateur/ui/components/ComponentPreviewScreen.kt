package com.example.dieselcalculateur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dieselcalculateur.ui.theme.AmberSecondary
import com.example.dieselcalculateur.ui.theme.DarkBackground
import com.example.dieselcalculateur.ui.theme.OffWhite
import com.example.dieselcalculateur.ui.theme.TealPrimary

/**
 * Écran de démonstration temporaire pour prévisualiser et tester
 * tous les nouveaux composants réutilisables "Pill & Glow" (Phase 9.1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentPreviewScreen(
    onBack: () -> Unit
) {
    var samplePrice by remember { mutableStateOf("1.759") }
    var sampleDistance by remember { mutableStateOf("450") }
    var selectedFuelIndex by remember { mutableIntStateOf(0) }
    val fuels = listOf("Gazole (B7)", "E10", "SP98", "E85")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Design System Preview",
                            color = OffWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Composants Pilule & Glow (Phase 9.1)",
                            color = OffWhite.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = OffWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- SECTION 1: GlowTextField ---
            PreviewSection(title = "1. GlowTextField (Champs Pilule avec Glow Teal)") {
                GlowTextField(
                    value = samplePrice,
                    onValueChange = { samplePrice = it },
                    label = "Prix du carburant",
                    placeholder = "ex: 1.759",
                    suffix = "€ / L",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlowTextField(
                    value = sampleDistance,
                    onValueChange = { sampleDistance = it },
                    label = "Distance du trajet",
                    placeholder = "ex: 450",
                    suffix = "km",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = OffWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            // --- SECTION 2: GlowCard ---
            PreviewSection(title = "2. GlowCard (Cartes avec bordure lumineuse)") {
                // Card Teal (Résultat de calcul)
                GlowCard(
                    type = GlowCardType.TEAL,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Coût estimé du trajet",
                            color = OffWhite.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "42,85 €",
                            color = TealPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Glow Teal - Utilisé pour les résultats principaux",
                            color = OffWhite.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card Amber (Station recommandée)
                GlowCard(
                    type = GlowCardType.AMBER,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Station la moins chère",
                            color = OffWhite.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "TotalEnergies Relais - 1.689 €/L",
                            color = AmberSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Glow Ambre - Utilisé pour les sélections et alertes",
                            color = OffWhite.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card Neutre (Historique)
                GlowCard(
                    type = GlowCardType.NEUTRAL,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Card Neutre (Ex: Historique ou reglages)",
                            color = OffWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bordure subtile sans glow intense",
                            color = OffWhite.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- SECTION 3: PillButton ---
            PreviewSection(title = "3. PillButton (Boutons Pilule)") {
                // Bouton Primaire Ambre
                PillButton(
                    text = "Enregistrer le calcul",
                    variant = PillButtonVariant.PRIMARY,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bouton Teal
                PillButton(
                    text = "Localiser les stations",
                    variant = PillButtonVariant.TEAL,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bouton Secondaire
                PillButton(
                    text = "Nouveau calcul",
                    variant = PillButtonVariant.SECONDARY,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- SECTION 4: PillSelectableButton ---
            PreviewSection(title = "4. PillSelectableButton (Sélecteur de Carburant)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fuels.forEachIndexed { index, fuel ->
                        PillSelectableButton(
                            text = fuel,
                            selected = (selectedFuelIndex == index),
                            onClick = { selectedFuelIndex = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = TealPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}
