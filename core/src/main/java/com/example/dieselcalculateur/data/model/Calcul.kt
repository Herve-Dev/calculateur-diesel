package com.example.dieselcalculateur.data.model

data class Calcul(
    val id: String = "",
    val date: Long,
    val montantSouhaite: Double,
    val prixPlafonne: Double,
    val prixAffiche: Double,
    val litres: Double,
    val montantAAnnoncer: Double,
    val economie: Double
)
