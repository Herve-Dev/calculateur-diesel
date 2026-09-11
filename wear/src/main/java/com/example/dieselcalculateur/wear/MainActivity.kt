package com.example.dieselcalculateur.wear

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.dieselcalculateur.data.model.CalculResult
import com.example.dieselcalculateur.data.model.CalculateurLogic
import com.example.dieselcalculateur.data.model.StationCarburant
import com.example.dieselcalculateur.wear.theme.DieselCalculateurWearTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DieselCalculateurWearTheme {
                val navController = rememberSwipeDismissableNavController()
                val viewModel: WearViewModel = viewModel()

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "stations"
                ) {
                    composable("stations") {
                        WearMainScreen(
                            viewModel = viewModel,
                            onStationClick = { station ->
                                val prix = station.prixGazole ?: 0.0
                                val enseigne = Uri.encode(station.enseigne ?: "Station")
                                val ville = Uri.encode(station.ville ?: "")
                                navController.navigate("montant/$prix/$enseigne/$ville")
                            }
                        )
                    }

                    composable("montant/{prix}/{enseigne}/{ville}") { backStackEntry ->
                        val prixStr = backStackEntry.arguments?.getString("prix") ?: "0.0"
                        val enseigneStr = backStackEntry.arguments?.getString("enseigne") ?: "Station"
                        val villeStr = backStackEntry.arguments?.getString("ville") ?: ""
                        
                        val prix = prixStr.toDoubleOrNull() ?: 0.0
                        val enseigne = Uri.decode(enseigneStr)
                        val ville = Uri.decode(villeStr)

                        MontantScreen(
                            stationPrix = prix,
                            stationEnseigne = enseigne,
                            stationVille = ville,
                            onValider = { montant ->
                                val encodedEnseigne = Uri.encode(enseigne)
                                val encodedVille = Uri.encode(ville)
                                navController.navigate("resultat/$montant/$prix/$encodedEnseigne/$encodedVille")
                            }
                        )
                    }

                    composable("resultat/{montant}/{prix}/{enseigne}/{ville}") { backStackEntry ->
                        val montantStr = backStackEntry.arguments?.getString("montant") ?: "20"
                        val prixStr = backStackEntry.arguments?.getString("prix") ?: "0.0"
                        val enseigneStr = backStackEntry.arguments?.getString("enseigne") ?: "Station"
                        val villeStr = backStackEntry.arguments?.getString("ville") ?: ""

                        val montant = montantStr.toIntOrNull() ?: 20
                        val prix = prixStr.toDoubleOrNull() ?: 0.0
                        val enseigne = Uri.decode(enseigneStr)
                        val ville = Uri.decode(villeStr)

                        ResultatScreen(
                            montant = montant,
                            stationPrix = prix,
                            stationEnseigne = enseigne,
                            stationVille = ville
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WearMainScreen(
    viewModel: WearViewModel,
    onStationClick: (StationCarburant) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.rechercherStations()
        }
    }

    fun checkAndStartLocation() {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocation || coarseLocation) {
            viewModel.rechercherStations()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
        timeText = {
            TimeText()
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "Stations Gazole",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            when (val state = uiState) {
                is WearUiState.Idle -> {
                    item {
                        Chip(
                            label = { Text("Localiser") },
                            onClick = { checkAndStartLocation() },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }
                }

                is WearUiState.Loading -> {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(top = 8.dp)
                        )
                    }
                    item {
                        Text(
                            text = "Recherche...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.onBackground
                        )
                    }
                }

                is WearUiState.Success -> {
                    items(state.stations, key = { it.id }) { station ->
                        StationWearItem(
                            station = station,
                            onClick = { onStationClick(station) }
                        )
                    }

                    item {
                        CompactChip(
                            label = { Text("Actualiser") },
                            onClick = { checkAndStartLocation() },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                is WearUiState.Empty -> {
                    item {
                        Text(
                            text = "Aucune station à proximité.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.onBackground,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    item {
                        Chip(
                            label = { Text("Réessayer") },
                            onClick = { checkAndStartLocation() },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }
                }

                is WearUiState.Error -> {
                    item {
                        Text(
                            text = state.message,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    item {
                        Chip(
                            label = { Text("Réessayer") },
                            onClick = { checkAndStartLocation() },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }
                }

                is WearUiState.PermissionRequired -> {
                    item {
                        Text(
                            text = "Permission GPS requise.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.error
                        )
                    }
                    item {
                        Chip(
                            label = { Text("Autoriser GPS") },
                            onClick = { checkAndStartLocation() },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StationWearItem(
    station: StationCarburant,
    onClick: () -> Unit
) {
    val priceAndDistance = String.format(
        Locale.FRANCE,
        "%.3f €/L • %.1f km",
        station.prixGazole ?: 0.0,
        station.distanceMetres / 1000.0
    )
    val ville = station.ville

    Chip(
        label = {
            Text(
                text = station.enseigne ?: "Station",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Column {
                Text(
                    text = priceAndDistance,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!ville.isNullOrBlank()) {
                    Text(
                        text = ville,
                        fontSize = 10.sp,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        onClick = onClick,
        colors = ChipDefaults.secondaryChipColors(),
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

@Composable
fun MontantScreen(
    stationPrix: Double,
    stationEnseigne: String,
    stationVille: String,
    onValider: (Int) -> Unit
) {
    var montant by remember { mutableIntStateOf(20) } // 20 € par défaut
    val listState = rememberScalingLazyListState()

    val headerTitle = if (stationVille.isNotBlank()) {
        "$stationEnseigne — $stationVille"
    } else {
        stationEnseigne
    }

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
        timeText = {
            TimeText()
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = headerTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                Text(
                    text = String.format(Locale.FRANCE, "Prix : %.3f €/L", stationPrix),
                    fontSize = 11.sp,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }

            // Section Montant au centre
            item {
                Text(
                    text = "$montant €",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Boutons Stepper +5 / -5
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactChip(
                        label = { Text("-5 €", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        onClick = { if (montant >= 5) montant -= 5 else montant = 0 },
                        colors = ChipDefaults.secondaryChipColors(),
                        enabled = montant > 0
                    )

                    CompactChip(
                        label = { Text("+5 €", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        onClick = { montant += 5 },
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }

            // Boutons Stepper +10 / -10
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactChip(
                        label = { Text("-10 €", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        onClick = { if (montant >= 10) montant -= 10 else montant = 0 },
                        colors = ChipDefaults.secondaryChipColors(),
                        enabled = montant > 0
                    )

                    CompactChip(
                        label = { Text("+10 €", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        onClick = { montant += 10 },
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }

            // Bouton Valider
            item {
                Chip(
                    label = { Text("Valider", fontWeight = FontWeight.Bold) },
                    onClick = { onValider(montant) },
                    colors = ChipDefaults.primaryChipColors(),
                    enabled = montant > 0,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ResultatScreen(
    montant: Int,
    stationPrix: Double,
    stationEnseigne: String,
    stationVille: String
) {
    val listState = rememberScalingLazyListState()

    // Calcul officiel via CalculateurLogic (:core) avec prix plafonné fixe à 1.99 €/L
    val result = remember(montant, stationPrix) {
        CalculateurLogic.calculer(
            montantSouhaite = montant.toString(),
            prixAffiche = stationPrix.toString(),
            prixPlafonne = "1.99"
        )
    }

    val headerText = if (stationVille.isNotBlank()) {
        "$stationEnseigne — $stationVille"
    } else {
        stationEnseigne
    }

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
        timeText = {
            TimeText()
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = headerText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            when (result) {
                is CalculResult.Success -> {
                    item {
                        Text(
                            text = "À ANNONCER AU CAISSIER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    item {
                        Text(
                            text = String.format(Locale.FRANCE, "%.2f €", result.montantAAnnoncer),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.secondary, // Ambre #F5C542
                            textAlign = TextAlign.Center
                        )
                    }

                    item {
                        Text(
                            text = String.format(Locale.FRANCE, "Volume : %.2f L", result.litres),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (result.economie > 0) {
                        item {
                            Text(
                                text = String.format(Locale.FRANCE, "Économie : %.2f €", result.economie),
                                fontSize = 11.sp,
                                color = MaterialTheme.colors.primary, // Teal #2DD4BF
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is CalculResult.Error -> {
                    item {
                        Text(
                            text = result.message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
