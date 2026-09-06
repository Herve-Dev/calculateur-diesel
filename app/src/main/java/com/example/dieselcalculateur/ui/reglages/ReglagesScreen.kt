package com.example.dieselcalculateur.ui.reglages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dieselcalculateur.data.model.Carburant
import com.example.dieselcalculateur.ui.calculateur.CalculateurViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReglagesScreen(
    viewModel: CalculateurViewModel,
    onBack: () -> Unit
) {
    val rayon by viewModel.rayonRecherche.collectAsState()
    val carburantSelectionne by viewModel.carburantSelectionne.collectAsState()

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
        }
    }
}
