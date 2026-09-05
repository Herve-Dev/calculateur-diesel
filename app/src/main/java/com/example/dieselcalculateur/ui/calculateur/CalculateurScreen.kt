package com.example.dieselcalculateur.ui.calculateur

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dieselcalculateur.data.model.CalculResult
import com.example.dieselcalculateur.ui.calculateur.CalculateurViewModel

@Composable
fun CalculateurScreen(
    viewModel: CalculateurViewModel = viewModel()
) {
    val montantSouhaite by viewModel.montantSouhaite.collectAsState()
    val prixAffiche by viewModel.prixAffiche.collectAsState()
    val prixPlafonne by viewModel.prixPlafonne.collectAsState()
    val resultat by viewModel.resultat.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = montantSouhaite,
            onValueChange = viewModel::onMontantSouhaiteChange,
            label = { Text("Montant réellement souhaité (€)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = prixAffiche,
            onValueChange = viewModel::onPrixAfficheChange,
            label = { Text("Prix affiché à la pompe (€/L)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = prixPlafonne,
            onValueChange = viewModel::onPrixPlafonneChange,
            label = { Text("Prix plafonné de la carte (€/L)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        // Affichage du résultat ou de l'erreur
        when (val res = resultat) {
            is CalculResult.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(java.util.Locale.FRANCE, "%.2f €", res.montantAAnnoncer),
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = String.format(java.util.Locale.FRANCE, "%.2f L", res.litres),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = String.format(java.util.Locale.FRANCE, "Économie réalisée : %.2f €", res.economie),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is CalculResult.Error -> {
                Text(
                    text = res.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            null -> { /* Rien ne s'affiche si aucun calcul n'a été tenté */ }
        }
    }
}