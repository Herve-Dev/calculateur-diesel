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
        radiusMeters: Int,
        fuelId: Int
    ): List<StationCarburant> {
        return try {
            val stationsRaw = api.getStationsAround(latitude, longitude)
            Log.d("FuelStationService", "Stations GPS reçues avant filtrage: ${stationsRaw.size}")
            
            mapToDomain(stationsRaw, fuelId, radiusMeters)
        } catch (e: Exception) {
            Log.e("FuelStationService", "Erreur GPS: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun searchStationsByText(
        query: String,
        fuelId: Int,
        radiusMeters: Int
    ): List<StationCarburant> {
        return try {
            val stationsRaw = api.searchStations(query)
            Log.d("FuelStationService", "Stations Texte reçues avant filtrage: ${stationsRaw.size}")
            
            mapToDomain(stationsRaw, fuelId, radiusMeters)
        } catch (e: Exception) {
            Log.e("FuelStationService", "Erreur Texte: ${e.message}", e)
            emptyList()
        }
    }

    private fun mapToDomain(
        dtos: List<FuelStationDto>,
        fuelId: Int,
        radiusMeters: Int
    ): List<StationCarburant> {
        Log.d("FuelStationService", "mapToDomain: début avec ${dtos.size} stations")
        
        val brandFiltered = dtos.filter { dto ->
            val isTargetBrand = dto.brand?.id in TARGET_BRAND_IDS
            if (!isTargetBrand) {
                Log.v("FuelStationService", "Station ${dto.id} rejetée: brand id ${dto.brand?.id} non ciblé")
            }
            isTargetBrand
        }
        Log.d("FuelStationService", "mapToDomain: après filtrage marque: ${brandFiltered.size} stations")

        val distanceFiltered = brandFiltered.filter { dto ->
            val distanceVal = dto.distance?.value
            val isDistanceOk = if (distanceVal != null) distanceVal <= radiusMeters else true
            if (!isDistanceOk) {
                Log.v("FuelStationService", "Station ${dto.id} rejetée: distance $distanceVal > $radiusMeters")
            }
            isDistanceOk
        }
        Log.d("FuelStationService", "mapToDomain: après filtrage distance: ${distanceFiltered.size} stations")

        return distanceFiltered.map { dto ->
            val fuel = dto.fuels?.find { it.id == fuelId }
            
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
                prixGazole = fuel?.price?.value,
                dateMiseAJour = fuel?.update?.value,
                horaires = if (dto.fuels?.any { it.available == true } == true) "Ouverte" else "Fermée",
                distanceMetres = dto.distance?.value ?: 0.0
            )
        }.sortedBy { it.distanceMetres }
    }
}
