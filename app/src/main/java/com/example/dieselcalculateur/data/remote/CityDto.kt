package com.example.dieselcalculateur.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityResponseDto(
    @Json(name = "features") val features: List<CityFeatureDto>
)

@JsonClass(generateAdapter = true)
data class CityFeatureDto(
    @Json(name = "geometry") val geometry: CityGeometryDto,
    @Json(name = "properties") val properties: CityPropertiesDto
)

@JsonClass(generateAdapter = true)
data class CityGeometryDto(
    @Json(name = "coordinates") val coordinates: List<Double> // [lon, lat]
)

@JsonClass(generateAdapter = true)
data class CityPropertiesDto(
    @Json(name = "label") val label: String,
    @Json(name = "city") val city: String,
    @Json(name = "postcode") val postcode: String,
    @Json(name = "context") val context: String
)
