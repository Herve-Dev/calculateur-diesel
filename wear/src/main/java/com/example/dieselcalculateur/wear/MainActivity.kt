package com.example.dieselcalculateur.wear

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.dieselcalculateur.data.model.StationCarburant
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: WearViewModel = viewModel()
                WearMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WearMainScreen(viewModel: WearViewModel) {
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
                        StationWearItem(station = station)
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
fun StationWearItem(station: StationCarburant) {
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
        onClick = { },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}
