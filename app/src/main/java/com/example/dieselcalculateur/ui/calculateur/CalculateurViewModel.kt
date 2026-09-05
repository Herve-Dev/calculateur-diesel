package com.example.dieselcalculateur.ui.calculateur

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieselcalculateur.data.local.AppDatabase
import com.example.dieselcalculateur.data.local.CalculEntity
import com.example.dieselcalculateur.data.model.CalculResult
import com.example.dieselcalculateur.data.model.CalculateurLogic
import com.example.dieselcalculateur.data.repository.CalculRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculateurViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CalculRepository(
        AppDatabase.getInstance(application).calculDao()
    )

    private val _montantSouhaite = MutableStateFlow("")
    val montantSouhaite: StateFlow<String> = _montantSouhaite.asStateFlow()

    private val _prixAffiche = MutableStateFlow("")
    val prixAffiche: StateFlow<String> = _prixAffiche.asStateFlow()

    private val _prixPlafonne = MutableStateFlow("1.99")
    val prixPlafonne: StateFlow<String> = _prixPlafonne.asStateFlow()

    private val _resultat = MutableStateFlow<CalculResult?>(null)
    val resultat: StateFlow<CalculResult?> = _resultat.asStateFlow()

    val historique: StateFlow<List<CalculEntity>> = repository.getHistorique()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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
        sauvegarderCalcul(nouveauResultat)
    }

    fun enregistrerCalcul() {
        _resultat.value?.let(::sauvegarderCalcul)
    }

    private fun sauvegarderCalcul(resultat: CalculResult) {
        if (resultat is CalculResult.Success) {
            viewModelScope.launch {
                repository.insererCalcul(resultat.toEntity())
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
