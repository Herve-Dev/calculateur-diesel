package com.example.dieselcalculateur.ui.calculateur

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dieselcalculateur.data.local.CalculEntity
import com.example.dieselcalculateur.data.model.CalculResult
import com.example.dieselcalculateur.data.model.StationCarburant
import com.example.dieselcalculateur.data.remote.CitySuggestion
import com.example.dieselcalculateur.ui.components.*
import com.example.dieselcalculateur.ui.stations.LocationPermissionHandler
import com.example.dieselcalculateur.ui.theme.AmberSecondary
import com.example.dieselcalculateur.ui.theme.OffWhite
import com.example.dieselcalculateur.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateurScreen(
    viewModel: CalculateurViewModel = viewModel(),
    onNavigateToReglages: () -> Unit
) {
    val montantSouhaite by viewModel.montantSouhaite.collectAsState()
    val prixAffiche by viewModel.prixAffiche.collectAsState()
    val prixPlafonne by viewModel.prixPlafonne.collectAsState()
    val resultat by viewModel.resultat.collectAsState()
    val historique by viewModel.historique.collectAsState()
    val stationsState by viewModel.stationsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val citySuggestions by viewModel.citySuggestions.collectAsState()

    var confirmationVisible by remember { mutableStateOf(false) }
    var showPermissionHandler by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    if (showPermissionHandler) {
        LocationPermissionHandler { isGranted ->
            showPermissionHandler = false
            viewModel.rechercherStations(isGranted)
        }
    }

    LaunchedEffect(confirmationVisible) {
        if (confirmationVisible) {
            delay(2_000)
            confirmationVisible = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculateur Diesel") },
                actions = {
                    IconButton(onClick = onNavigateToReglages) {
                        Icon(Icons.Default.Settings, contentDescription = "Réglages")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Formulaire de saisie réutilisant GlowTextField
                GlowTextField(
                    value = montantSouhaite,
                    onValueChange = viewModel::onMontantSouhaiteChange,
                    label = "Montant réellement souhaité",
                    placeholder = "ex: 50.00",
                    suffix = "€",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                GlowTextField(
                    value = prixAffiche,
                    onValueChange = viewModel::onPrixAfficheChange,
                    label = "Prix affiché à la pompe",
                    placeholder = "ex: 1.759",
                    suffix = "€ / L",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                GlowTextField(
                    value = prixPlafonne,
                    onValueChange = viewModel::onPrixPlafonneChange,
                    label = "Prix plafonné de la carte",
                    placeholder = "ex: 1.689",
                    suffix = "€ / L",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Affichage du résultat ou de l'erreur avec GlowCard & PillButton
                when (val res = resultat) {
                    is CalculResult.Success -> {
                        GlowCard(
                            type = GlowCardType.TEAL,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "À ANNONCER AU CAISSIER :",
                                    color = OffWhite.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format(Locale.FRANCE, "%.2f €", res.montantAAnnoncer),
                                    color = TealPrimary,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.FRANCE, "Volume : %.2f L", res.litres),
                                    color = OffWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (res.economie > 0) {
                                    Text(
                                        text = String.format(Locale.FRANCE, "Économie : %.2f €", res.economie),
                                        color = AmberSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PillButton(
                                        text = if (confirmationVisible) "✓ Enregistré" else "Enregistrer",
                                        variant = PillButtonVariant.PRIMARY,
                                        onClick = {
                                            viewModel.enregistrerCalcul()
                                            confirmationVisible = true
                                        },
                                        enabled = !confirmationVisible,
                                        modifier = Modifier.weight(1f)
                                    )

                                    PillButton(
                                        text = "Nouveau",
                                        variant = PillButtonVariant.SECONDARY,
                                        onClick = viewModel::nouveauCalcul,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
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

                // Section Stations Proches (inchangée à cette étape)
                Text(
                    text = "Stations à proximité",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                SearchTextField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onMyLocationClick = {
                        focusManager.clearFocus()
                        showPermissionHandler = true
                    },
                    onSearchAction = {
                        focusManager.clearFocus()
                        viewModel.rechercherStationsParTexte()
                    }
                )

                if (citySuggestions.isNotEmpty()) {
                    Popup(
                        alignment = Alignment.TopCenter,
                        onDismissRequest = { viewModel.onSearchQueryChange(searchQuery) }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .heightIn(max = 200.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            LazyColumn {
                                items(citySuggestions) { suggestion ->
                                    ListItem(
                                        headlineContent = { Text(suggestion.city) },
                                        supportingContent = { Text("${suggestion.postcode} - ${suggestion.context}") },
                                        modifier = Modifier.clickable {
                                            focusManager.clearFocus()
                                            viewModel.selectCity(suggestion)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                when (val state = stationsState) {
                    is StationsState.Idle -> {
                        // Le champ de recherche est déjà affiché au-dessus
                    }
                    is StationsState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is StationsState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.stations, key = { it.id }) { station ->
                                StationItem(
                                    station = station,
                                    onClick = { viewModel.selectionnerStation(station) }
                                )
                            }
                        }
                        TextButton(
                            onClick = { viewModel.recommencerRecherche() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Effacer les résultats")
                        }
                    }
                    is StationsState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { viewModel.recommencerRecherche() }) {
                                Text("Recommencer")
                            }
                        }
                    }
                    is StationsState.PermissionRequired -> {
                        Text(
                            "La permission de localisation est nécessaire pour trouver les stations autour de vous.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { showPermissionHandler = true }) {
                            Text("Accorder la permission")
                        }
                    }
                }

                // Section Historique (inchangée à cette étape)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historique",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (historique.isNotEmpty()) {
                        TextButton(onClick = viewModel::viderHistorique) {
                            Text("Vider")
                        }
                    }
                }

                if (historique.isEmpty()) {
                    Text(
                        text = "Aucun calcul enregistré pour le moment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historique, key = { it.id }) { calcul ->
                            val isNew = confirmationVisible && calcul == historique.firstOrNull()
                            CalculHistoriqueItem(
                                calcul = calcul,
                                isNew = isNew
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMyLocationClick: () -> Unit,
    onSearchAction: () -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Ville ou Code Postal") },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onMyLocationClick) {
                Icon(Icons.Default.MyLocation, contentDescription = "Ma position")
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchAction() }
        ),
        singleLine = true
    )
}

@Composable
private fun StationItem(
    station: StationCarburant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = station.enseigne ?: "Station",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = String.format(Locale.FRANCE, "%.2f €/L", station.prixGazole ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "${station.adresse}, ${station.codePostal ?: ""} ${station.ville ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    station.horaires?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    station.dateMiseAJour?.let {
                        Text(
                            text = "MàJ : ${formatDateIso(it)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = String.format(Locale.FRANCE, "À %.1f km", station.distanceMetres / 1000.0),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun formatDateIso(isoString: String): String {
    return try {
        val datePart = isoString.split("T").firstOrNull() ?: isoString
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            datePart
        }
    } catch (e: Exception) {
        isoString
    }
}

@Composable
private fun CalculHistoriqueItem(
    calcul: CalculEntity,
    isNew: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNew) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatDate(calcul.date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format(Locale.FRANCE, "Montant souhaité : %.2f €", calcul.montantSouhaite),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format(Locale.FRANCE, "Litres : %.2f L", calcul.litres),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format(Locale.FRANCE, "Montant à annoncer : %.2f €", calcul.montantAAnnoncer),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
        Locale.FRANCE
    )
    return formatter.format(Date(timestamp))
}
