package com.example.dieselcalculateur.data.model

sealed interface CalculResult {
    data class Success(
        val litres: Double,
        val montantAAnnoncer: Double,
        val economie: Double
    ) : CalculResult

    data class Error(val message: String) : CalculResult
}
