package com.example.dieselcalculateur.data.model

data class StationCarburant(
    val id: String,
    val adresse: String,
    val codePostal: String?,
    val ville: String?,
    val latitude: Double,
    val longitude: Double,
    val enseigne: String?,
    val prixGazole: Double?,
    val dateMiseAJour: String?,
    val horaires: String?,
    val distanceMetres: Double = 0.0
)
