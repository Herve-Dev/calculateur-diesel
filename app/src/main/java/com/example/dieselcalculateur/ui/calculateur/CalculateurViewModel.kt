package com.example.dieselcalculateur.ui.calculateur

import androidx.lifecycle.ViewModel
import com.example.dieselcalculateur.data.model.CalculResult
import com.example.dieselcalculateur.data.model.CalculateurLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculateurViewModel : ViewModel() {

    private val _montantSouhaite = MutableStateFlow("")
    val montantSouhaite: StateFlow<String> = _montantSouhaite.asStateFlow()

    private val _prixAffiche = MutableStateFlow("")
    val prixAffiche: StateFlow<String> = _prixAffiche.asStateFlow()

    private val _prixPlafonne = MutableStateFlow("1.99")
    val prixPlafonne: StateFlow<String> = _prixPlafonne.asStateFlow()

    private val _resultat = MutableStateFlow<CalculResult?>(null)
    val resultat: StateFlow<CalculResult?> = _resultat.asStateFlow()

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
        _resultat.value = CalculateurLogic.calculer(
            _montantSouhaite.value,
            _prixAffiche.value,
            _prixPlafonne.value
        )
    }
}
