package com.example.dieselcalculateur.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FuelStationDto(
    @Json(name = "id") val id: String,
    @Json(name = "latitude") val latitude: String?,
    @Json(name = "longitude") val longitude: String?,
    @Json(name = "adresse") val adresse: String?,
    @Json(name = "ville") val ville: String?,
    @Json(name = "cp") val cp: String?,
    @Json(name = "nom") val nom: String?,
    @Json(name = "prix_gazole") val prixGazole: Double?,
    @Json(name = "gazole_maj") val gazoleMaj: String?
)

@JsonClass(generateAdapter = true)
data class FuelStationResponse(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "results") val results: List<FuelStationDto>
)
