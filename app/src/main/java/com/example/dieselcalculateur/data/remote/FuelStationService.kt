package com.example.dieselcalculateur.data.remote

import android.util.Log
import com.example.dieselcalculateur.data.model.StationCarburant
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class FuelStationService {
    private val api: FuelStationApi

    companion object {
        private val TARGET_BRAND_IDS = setOf(1, 2) // TotalEnergies et TotalEnergies Access
        private const val FUEL_GAZOLE_ID = 1
    }

    init {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("FuelStationHTTP", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.prix-carburants.2aaz.fr/")
            .client(client)
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
            val stationsRaw = api.getStationsAround(latitude, longitude)
            
            Log.d("FuelStationService", "Stations reçues avant filtrage: ${stationsRaw.size}")
            
            stationsRaw.filter { dto ->
                val distanceVal = dto.distance?.value ?: Double.MAX_VALUE
                dto.brand?.id in TARGET_BRAND_IDS && distanceVal <= radiusMeters
            }.map { dto ->
                val gazoleFuel = dto.fuels?.find { it.id == FUEL_GAZOLE_ID }
                
                // Extraction code postal et ville depuis city_line (ex: "77170 Brie-Comte-Robert")
                val cityLine = dto.address?.cityLine ?: ""
                val cp = cityLine.split(" ").firstOrNull()
                val ville = cityLine.substringAfter(cp ?: "").trim()

                StationCarburant(
                    id = dto.id.toString(),
                    adresse = dto.address?.street ?: "",
                    codePostal = cp,
                    ville = ville,
                    latitude = dto.coordinates?.latitude ?: 0.0,
                    longitude = dto.coordinates?.longitude ?: 0.0,
                    enseigne = dto.brand?.name ?: "Total",
                    prixGazole = gazoleFuel?.price?.value,
                    dateMiseAJour = gazoleFuel?.update?.value,
                    horaires = if (dto.fuels?.any { it.available == true } == true) "Ouverte" else "Fermée",
                    distanceMetres = dto.distance?.value ?: 0.0
                )
            }.sortedBy { it.distanceMetres }
        } catch (e: Exception) {
            Log.e("FuelStationService", "Erreur lors de la récupération des stations: ${e.message}", e)
            emptyList()
        }
    }
}
