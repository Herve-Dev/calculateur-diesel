package com.example.dieselcalculateur.ui.reglages

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dieselcalculateur.data.model.Carburant
import com.example.dieselcalculateur.ui.calculateur.CalculateurViewModel
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
                title = { Text("Réglages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section Rayon
            Column {
                Text(
                    text = "Rayon de recherche : ${rayon.toInt()} km",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = rayon,
                    onValueChange = { newValue ->
                        // On arrondit au multiple de 5 le plus proche pour plus de sécurité
                        val roundedValue = (Math.round(newValue / 5f) * 5).toFloat()
                        viewModel.onRayonRechercheChange(roundedValue)
                    },
                    valueRange = 5f..30f,
                    steps = 4 // (30-5)/5 - 1 = 4 steps (10, 15, 20, 25)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5 km", style = MaterialTheme.typography.labelSmall)
                    Text("30 km", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Section Carburant
            Column {
                Text(
                    text = "Type de carburant",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Carburant.entries.forEach { carburant ->
                        FilterChip(
                            selected = carburant == carburantSelectionne,
                            onClick = { viewModel.onCarburantSelectionneChange(carburant) },
                            label = { Text(carburant.nom) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Section À propos / Mise à jour
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Version : ${AppVersion.getVersionName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (val state = updateState) {
                    is UpdateState.Idle, is UpdateState.UpToDate, is UpdateState.Error -> {
                        Button(
                            onClick = { updateViewModel.verifierMiseAJour() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Vérifier les mises à jour")
                        }
                        if (state is UpdateState.UpToDate) {
                            Text(
                                "Application à jour",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (state is UpdateState.Error) {
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                    is UpdateState.Checking -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is UpdateState.UpdateAvailable -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Nouvelle version disponible : ${state.latestVersion}",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
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
                            ) {
                                Text("Télécharger et installer")
                            }
                        }
                    }
                    is UpdateState.Downloading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Téléchargement : ${state.progress}%", style = MaterialTheme.typography.bodySmall)
                            LinearProgressIndicator(
                                progress = { state.progress / 100f },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                    is UpdateState.ReadyToInstall -> {
                        Button(
                            onClick = { updateViewModel.installerApk() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Installer maintenant")
                        }
                    }
                }
            }
        }
    }
}
