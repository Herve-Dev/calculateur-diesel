package com.example.dieselcalculateur.data.model

data class Station(
    val nom: String,
    val marque: String,
    val typeTotalEnergies: String,
    val adresse: String,
    val ville: String,
    val distanceKm: Double,
    val prixGazole: Double?,
    val dateMiseAJour: String
)
