package com.example.dieselcalculateur.ui.reglages

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dieselcalculateur.data.model.Carburant
import com.example.dieselcalculateur.ui.calculateur.CalculateurViewModel
import com.example.dieselcalculateur.ui.components.*
import com.example.dieselcalculateur.ui.theme.AmberSecondary
import com.example.dieselcalculateur.ui.theme.DarkBackground
import com.example.dieselcalculateur.ui.theme.OffWhite
import com.example.dieselcalculateur.ui.theme.TealPrimary
import com.example.dieselcalculateur.ui.update.UpdateState
import com.example.dieselcalculateur.ui.update.UpdateViewModel
import com.example.dieselcalculateur.util.AppVersion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReglagesScreen(
    viewModel: CalculateurViewModel,
    updateViewModel: UpdateViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val rayon by viewModel.rayonRecherche.collectAsState()
    val carburantSelectionne by viewModel.carburantSelectionne.collectAsState()
    val updateState by updateViewModel.updateState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Réglages",
                        color = OffWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1 : Type de Carburant (GlowCard + PillSelectableButton)
            GlowCard(
                type = GlowCardType.NEUTRAL,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TYPE DE CARBURANT",
                        color = OffWhite.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Carburant.entries.forEach { carburant ->
                            PillSelectableButton(
                                text = carburant.nom,
                                selected = (carburant == carburantSelectionne),
                                onClick = { viewModel.onCarburantSelectionneChange(carburant) }
                            )
                        }
                    }
                }
            }

            // Section 2 : Rayon de recherche (GlowCard + Slider personnalisé)
            GlowCard(
                type = GlowCardType.NEUTRAL,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RAYON DE RECHERCHE",
                            color = OffWhite.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${rayon.toInt()} km",
                            color = TealPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = rayon,
                        onValueChange = { newValue ->
                            val roundedValue = (Math.round(newValue / 5f) * 5).toFloat()
                            viewModel.onRayonRechercheChange(roundedValue)
                        },
                        valueRange = 5f..30f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = TealPrimary,
                            activeTrackColor = TealPrimary,
                            inactiveTrackColor = Color(0xFF2E3A46)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5 km", color = OffWhite.copy(alpha = 0.4f), fontSize = 11.sp)
                        Text("30 km", color = OffWhite.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                }
            }

            // Section 3 : Version & Mises à jour (GlowCard + PillButton)
            GlowCard(
                type = GlowCardType.NEUTRAL,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "À PROPOS & MISES À JOUR",
                        color = OffWhite.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = "Version actuelle : ${AppVersion.getVersionName()}",
                        color = OffWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    when (val state = updateState) {
                        is UpdateState.Idle, is UpdateState.UpToDate, is UpdateState.Error -> {
                            PillButton(
                                text = "Vérifier les mises à jour",
                                variant = PillButtonVariant.PRIMARY,
                                onClick = { updateViewModel.verifierMiseAJour() },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (state is UpdateState.UpToDate) {
                                Text(
                                    text = "✓ Application à jour",
                                    color = TealPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else if (state is UpdateState.Error) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                        is UpdateState.Checking -> {
                            CircularProgressIndicator(
                                color = TealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is UpdateState.UpdateAvailable -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Nouvelle version disponible : ${state.latestVersion}",
                                    color = AmberSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!context.packageManager.canRequestPackageInstalls()) {
                                    Text(
                                        text = "Autorisation d'installation requise",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                PillButton(
                                    text = if (context.packageManager.canRequestPackageInstalls()) "Télécharger et installer" else "Autoriser l'installation",
                                    variant = PillButtonVariant.PRIMARY,
                                    onClick = {
                                        if (!context.packageManager.canRequestPackageInstalls()) {
                                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            updateViewModel.telechargerMiseAJour(state.downloadUrl)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        is UpdateState.Downloading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val progressText = if (state.progress >= 0) "Téléchargement : ${state.progress}%" else "Téléchargement en cours..."
                                Text(
                                    text = progressText,
                                    color = OffWhite,
                                    fontSize = 13.sp
                                )
                                LinearProgressIndicator(
                                    progress = { if (state.progress >= 0) state.progress / 100f else 0f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = TealPrimary,
                                    trackColor = Color(0xFF2E3A46)
                                )
                            }
                        }
                        is UpdateState.ReadyToInstall -> {
                            PillButton(
                                text = "Installer maintenant",
                                variant = PillButtonVariant.PRIMARY,
                                onClick = { updateViewModel.installerApk() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
