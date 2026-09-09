package com.example.dieselcalculateur.ui.calculateur

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.dieselcalculateur.ui.theme.DarkBackground
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
                    .verticalScroll(rememberScrollState())
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

                // Section Stations Proches (Composants Pilule & Glow)
                Text(
                    text = "Stations à proximité",
                    color = OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
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
                            shape = RoundedCornerShape(12.dp),
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
                        CircularProgressIndicator(
                            color = TealPrimary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is StationsState.Success -> {
                        val cheapestPrice = state.stations
                            .mapNotNull { it.prixGazole }
                            .minOrNull()

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.stations, key = { it.id }) { station ->
                                val isCheapest = cheapestPrice != null && station.prixGazole == cheapestPrice
                                StationItem(
                                    station = station,
                                    isCheapest = isCheapest,
                                    onClick = { viewModel.selectionnerStation(station) }
                                )
                            }
                        }
                        TextButton(
                            onClick = { viewModel.recommencerRecherche() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Effacer les résultats", color = TealPrimary)
                        }
                    }
                    is StationsState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { viewModel.recommencerRecherche() }) {
                                Text("Recommencer", color = TealPrimary)
                            }
                        }
                    }
                    is StationsState.PermissionRequired -> {
                        Text(
                            "La permission de localisation est nécessaire pour trouver les stations autour de vous.",
                            color = MaterialTheme.colorScheme.error
                        )
                        PillButton(
                            text = "Accorder la permission",
                            variant = PillButtonVariant.TEAL,
                            onClick = { showPermissionHandler = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Section Historique (Scroll Horizontal avec GlowCard - Phase 9.4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historique",
                        color = OffWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (historique.isNotEmpty()) {
                        TextButton(onClick = viewModel::viderHistorique) {
                            Text("Vider", color = TealPrimary)
                        }
                    }
                }

                if (historique.isEmpty()) {
                    Text(
                        text = "Aucun calcul enregistré pour le moment",
                        color = OffWhite.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
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
    GlowTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = "Ville ou Code Postal",
        placeholder = "ex: Paris, 75001...",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = onMyLocationClick) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Ma position",
                    tint = AmberSecondary,
                    modifier = Modifier.size(20.dp)
                )
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
    isCheapest: Boolean = false,
    onClick: () -> Unit
) {
    GlowCard(
        type = if (isCheapest) GlowCardType.AMBER else GlowCardType.NEUTRAL,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = station.enseigne ?: "Station",
                        color = OffWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCheapest) {
                        Box(
                            modifier = Modifier
                                .background(AmberSecondary, shape = CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MOINS CHÈRE",
                                color = DarkBackground,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Text(
                    text = String.format(Locale.FRANCE, "%.3f €/L", station.prixGazole ?: 0.0),
                    color = if (isCheapest) AmberSecondary else TealPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${station.adresse}, ${station.codePostal ?: ""} ${station.ville ?: ""}",
                color = OffWhite.copy(alpha = 0.65f),
                fontSize = 13.sp
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
                            color = TealPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    station.dateMiseAJour?.let {
                        Text(
                            text = "MàJ : ${formatDateIso(it)}",
                            color = OffWhite.copy(alpha = 0.45f),
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = String.format(Locale.FRANCE, "À %.1f km", station.distanceMetres / 1000.0),
                    color = OffWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
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
    GlowCard(
        type = if (isNew) GlowCardType.TEAL else GlowCardType.NEUTRAL,
        modifier = Modifier.width(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatDate(calcul.date),
                color = OffWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "À ANNONCER",
                color = TealPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = String.format(Locale.FRANCE, "%.2f €", calcul.montantAAnnoncer),
                color = TealPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = String.format(Locale.FRANCE, "Souhaité : %.2f €", calcul.montantSouhaite),
                color = OffWhite.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            Text(
                text = String.format(Locale.FRANCE, "Volume : %.2f L", calcul.litres),
                color = OffWhite.copy(alpha = 0.6f),
                fontSize = 11.sp
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
