package com.example.dieselcalculateur.ui.reglages

import android.content.Intent
import android.net.Uri
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
                    onValueChange = viewModel::onRayonRechercheChange,
                    valueRange = 5f..30f,
                    steps = 5 // 5, 10, 15, 20, 25, 30
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
                                color = MaterialTheme.colorScheme.error
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
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.downloadUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Télécharger")
                            }
                        }
                    }
                }
            }
        }
    }
}
