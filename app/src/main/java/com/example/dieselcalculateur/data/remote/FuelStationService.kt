package com.example.dieselcalculateur.data.remote

import com.example.dieselcalculateur.data.model.StationCarburant
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.math.*

class FuelStationService {
    private val api: FuelStationApi

    init {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://data.economie.gouv.fr/api/explore/v2.1/catalog/datasets/prix-des-carburants-en-france-flux-instantane-v2/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(FuelStationApi::class.java)
    }

    suspend fun getNearbyStations(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): List<StationCarburant> {
        return try {
            val geofilter = "$latitude,$longitude,$radiusMeters"
            val response = api.getStations(geofilterDistance = geofilter)
            
            response.results.map { dto ->
                val stationLat = dto.latitude?.toDoubleOrNull() ?: 0.0
                val stationLon = dto.longitude?.toDoubleOrNull() ?: 0.0
                val distance = calculateDistance(latitude, longitude, stationLat, stationLon)
                
                StationCarburant(
                    id = dto.id,
                    adresse = dto.adresse ?: "",
                    latitude = stationLat,
                    longitude = stationLon,
                    enseigne = dto.nom ?: dto.ville ?: "Station Inconnue",
                    prixGazole = dto.prixGazole,
                    dateMiseAJour = dto.gazoleMaj,
                    distanceMetres = distance
                )
            }.sortedBy { it.distanceMetres }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Rayon de la Terre en mètres
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
