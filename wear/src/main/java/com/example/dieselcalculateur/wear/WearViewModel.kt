package com.example.dieselcalculateur.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieselcalculateur.data.local.SyncedCalculResult
import com.example.dieselcalculateur.data.local.WearDataStore
import com.example.dieselcalculateur.data.model.Carburant
import com.example.dieselcalculateur.data.model.StationCarburant
import com.example.dieselcalculateur.data.remote.FuelStationService
import com.example.dieselcalculateur.data.remote.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WearUiState {
    object Idle : WearUiState
    object Loading : WearUiState
    data class Success(val stations: List<StationCarburant>) : WearUiState
    object Empty : WearUiState
    data class Error(val message: String) : WearUiState
    object PermissionRequired : WearUiState
}

class WearViewModel(application: Application) : AndroidViewModel(application) {

    private val locationHelper = LocationHelper(application)
    private val fuelStationService = FuelStationService()
    private val wearDataStore = WearDataStore(application)

    val latestSyncedResult: StateFlow<SyncedCalculResult?> = wearDataStore.latestResultFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow<WearUiState>(WearUiState.Idle)
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    fun onLocaliserClick(hasLocationPermission: Boolean) {
        if (!hasLocationPermission) {
            _uiState.value = WearUiState.PermissionRequired
            return
        }

        rechercherStations()
    }

    fun rechercherStations() {
        viewModelScope.launch {
            _uiState.value = WearUiState.Loading

            val location = locationHelper.getLastKnownLocation()
            if (location == null) {
                _uiState.value = WearUiState.Error("Impossible d'obtenir la position GPS.")
                return@launch
            }

            val (lat, lon) = location
            val stations = fuelStationService.getNearbyStations(
                latitude = lat,
                longitude = lon,
                radiusMeters = 10000, // 10 km par défaut
                fuelId = Carburant.GAZOLE.id // Gazole par défaut
            )

            if (stations.isEmpty()) {
                _uiState.value = WearUiState.Empty
            } else {
                _uiState.value = WearUiState.Success(stations)
            }
        }
    }
}
