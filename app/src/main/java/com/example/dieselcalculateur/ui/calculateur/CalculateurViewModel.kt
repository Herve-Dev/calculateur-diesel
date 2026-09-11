package com.example.dieselcalculateur.ui.calculateur

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieselcalculateur.data.local.AppDatabase
import com.example.dieselcalculateur.data.local.CalculEntity
import com.example.dieselcalculateur.data.local.SettingsDataStore
import com.example.dieselcalculateur.data.model.CalculResult
import com.example.dieselcalculateur.data.model.CalculateurLogic
import com.example.dieselcalculateur.data.model.Carburant
import com.example.dieselcalculateur.data.model.StationCarburant
import com.example.dieselcalculateur.data.remote.CityService
import com.example.dieselcalculateur.data.remote.CitySuggestion
import com.example.dieselcalculateur.data.remote.FuelStationService
import com.example.dieselcalculateur.data.remote.LocationHelper
import com.example.dieselcalculateur.data.repository.CalculRepository
import com.example.dieselcalculateur.util.WearDataSyncHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class StationsState {
    object Idle : StationsState()
    object Loading : StationsState()
    data class Success(val stations: List<StationCarburant>) : StationsState()
    data class Error(val message: String) : StationsState()
    object PermissionRequired : StationsState()
}

class CalculateurViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CalculRepository(
        AppDatabase.getInstance(application).calculDao()
    )
    private val locationHelper = LocationHelper(application)
    private val fuelStationService = FuelStationService()
    private val settingsDataStore = SettingsDataStore(application)
    private val cityService = CityService()

    private val _montantSouhaite = MutableStateFlow("")
    val montantSouhaite: StateFlow<String> = _montantSouhaite.asStateFlow()

    private val _prixAffiche = MutableStateFlow("")
    val prixAffiche: StateFlow<String> = _prixAffiche.asStateFlow()

    private val _prixPlafonne = MutableStateFlow("1.99")
    val prixPlafonne: StateFlow<String> = _prixPlafonne.asStateFlow()

    private val _resultat = MutableStateFlow<CalculResult?>(null)
    val resultat: StateFlow<CalculResult?> = _resultat.asStateFlow()

    private val _stationsState = MutableStateFlow<StationsState>(StationsState.Idle)
    val stationsState: StateFlow<StationsState> = _stationsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _citySuggestions = MutableStateFlow<List<CitySuggestion>>(emptyList())
    val citySuggestions: StateFlow<List<CitySuggestion>> = _citySuggestions.asStateFlow()

    val rayonRecherche: StateFlow<Float> = settingsDataStore.settingsFlow
        .map { it.rayon }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 10f
        )

    val carburantSelectionne: StateFlow<Carburant> = settingsDataStore.settingsFlow
        .map { it.carburant }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Carburant.GAZOLE
        )

    val historique: StateFlow<List<CalculEntity>> = repository.getHistorique()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    _citySuggestions.value = cityService.getCitySuggestions(query)
                }
        }
    }

    fun onMontantSouhaiteChange(newValue: String) {
        _montantSouhaite.value = newValue
        lancerCalcul()
    }

    fun onPrixAfficheChange(newValue: String) {
        _prixAffiche.value = newValue
        lancerCalcul()
    }

    fun onPrixPlafonneChange(newValue: String) {
        _prixPlafonne.value = newValue
        lancerCalcul()
    }

    private fun lancerCalcul() {
        val nouveauResultat = CalculateurLogic.calculer(
            _montantSouhaite.value,
            _prixAffiche.value,
            _prixPlafonne.value
        )
        _resultat.value = nouveauResultat
    }

    fun enregistrerCalcul() {
        _resultat.value?.let(::sauvegarderCalcul)
    }

    fun nouveauCalcul() {
        _montantSouhaite.value = ""
        _prixAffiche.value = ""
        _resultat.value = null
    }

    fun viderHistorique() {
        viewModelScope.launch {
            repository.supprimerTout()
        }
    }

    fun rechercherStations(permissionAccordee: Boolean = true) {
        if (!permissionAccordee) {
            _stationsState.value = StationsState.PermissionRequired
            return
        }

        viewModelScope.launch {
            _stationsState.value = StationsState.Loading
            Log.d("CalculateurVM", "Début de la récupération de position...")
            val location = locationHelper.getLastKnownLocation()
            Log.d("CalculateurVM", "Position reçue: $location")

            if (location != null) {
                val currentRayon = rayonRecherche.value
                val currentFuel = carburantSelectionne.value
                val stations = fuelStationService.getNearbyStations(
                    latitude = location.first,
                    longitude = location.second,
                    radiusMeters = (currentRayon * 1000).toInt(),
                    fuelId = currentFuel.id
                )
                if (stations.isEmpty()) {
                    _stationsState.value = StationsState.Error("Aucune station trouvée dans un rayon de ${currentRayon.toInt()}km")
                } else {
                    _stationsState.value = StationsState.Success(stations)
                }
            } else {
                _stationsState.value = StationsState.Error("Impossible de récupérer votre position")
            }
        }
    }

    fun selectionnerStation(station: StationCarburant) {
        station.prixGazole?.let { prix ->
            _prixAffiche.value = prix.toString()
            lancerCalcul()
            _stationsState.value = StationsState.Idle
        }
    }

    fun onRayonRechercheChange(newValue: Float) {
        viewModelScope.launch {
            settingsDataStore.saveRayon(newValue)
        }
    }

    fun onCarburantSelectionneChange(newValue: Carburant) {
        viewModelScope.launch {
            settingsDataStore.saveCarburant(newValue)
            _stationsState.value = StationsState.Idle // On reset la recherche si le carburant change
        }
    }

    fun onSearchQueryChange(newValue: String) {
        _searchQuery.value = newValue
        if (newValue.length < 2) {
            _citySuggestions.value = emptyList()
        }
    }

    fun selectCity(city: CitySuggestion) {
        _searchQuery.value = city.label
        _citySuggestions.value = emptyList()
        
        viewModelScope.launch {
            _stationsState.value = StationsState.Loading
            val stations = fuelStationService.getNearbyStations(
                latitude = city.latitude,
                longitude = city.longitude,
                radiusMeters = (rayonRecherche.value * 1000).toInt(),
                fuelId = carburantSelectionne.value.id
            )
            updateStationsResult(stations, city.label)
        }
    }

    fun rechercherStationsParTexte() {
        val query = _searchQuery.value
        if (query.length < 2) return
        _citySuggestions.value = emptyList()

        viewModelScope.launch {
            _stationsState.value = StationsState.Loading
            Log.d("CalculateurVM", "Recherche texte lancée pour query: '$query'")
            val stations = fuelStationService.searchStationsByText(
                query = query,
                fuelId = carburantSelectionne.value.id,
                radiusMeters = (rayonRecherche.value * 1000).toInt()
            )
            updateStationsResult(stations, query)
        }
    }

    private fun updateStationsResult(stations: List<StationCarburant>, query: String) {
        if (stations.isEmpty()) {
            _stationsState.value = StationsState.Error("Aucune station trouvée pour '$query'")
        } else {
            _stationsState.value = StationsState.Success(stations)
        }
    }

    fun recommencerRecherche() {
        _searchQuery.value = ""
        _citySuggestions.value = emptyList()
        _stationsState.value = StationsState.Idle
    }

    private fun sauvegarderCalcul(resultat: CalculResult) {
        if (resultat is CalculResult.Success) {
            viewModelScope.launch {
                repository.insererCalcul(resultat.toEntity())
                
                // Envoi silencieux vers la montre via Wear Data Layer API
                WearDataSyncHelper.syncLatestCalcul(
                    context = getApplication(),
                    montantAAnnoncer = resultat.montantAAnnoncer,
                    litres = resultat.litres,
                    economie = resultat.economie,
                    montantSouhaite = _montantSouhaite.value.toDoubleOrNull() ?: 0.0
                )
            }
        }
    }

    private fun CalculResult.Success.toEntity(): CalculEntity {
        return CalculEntity(
            date = System.currentTimeMillis(),
            montantSouhaite = _montantSouhaite.value.toDoubleOrNull() ?: 0.0,
            prixPlafonne = _prixPlafonne.value.toDoubleOrNull() ?: 0.0,
            prixAffiche = _prixAffiche.value.toDoubleOrNull() ?: 0.0,
            litres = litres,
            montantAAnnoncer = montantAAnnoncer,
            economie = economie
        )
    }
}
