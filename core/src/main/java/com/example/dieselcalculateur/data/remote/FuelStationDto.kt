package com.example.dieselcalculateur.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FuelStationDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "Brand") val brand: BrandDto?,
    @Json(name = "Address") val address: AddressDto?,
    @Json(name = "Coordinates") val coordinates: CoordinatesDto?,
    @Json(name = "Fuels") val fuels: List<FuelPriceDto>?,
    @Json(name = "Distance") val distance: DistanceDto?
)

@JsonClass(generateAdapter = true)
data class BrandDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class AddressDto(
    @Json(name = "street_line") val street: String?,
    @Json(name = "city_line") val cityLine: String?
)

@JsonClass(generateAdapter = true)
data class CoordinatesDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

@JsonClass(generateAdapter = true)
data class FuelPriceDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "Price") val price: PriceValueDto?,
    @Json(name = "Update") val update: UpdateDto?,
    @Json(name = "available") val available: Boolean?
)

@JsonClass(generateAdapter = true)
data class PriceValueDto(
    @Json(name = "value") val value: Double?
)

@JsonClass(generateAdapter = true)
data class UpdateDto(
    @Json(name = "value") val value: String?
)

@JsonClass(generateAdapter = true)
data class DistanceDto(
    @Json(name = "value") val value: Double?
)
