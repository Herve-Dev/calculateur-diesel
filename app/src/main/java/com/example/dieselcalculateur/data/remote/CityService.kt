package com.example.dieselcalculateur.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CityService {
    private val api: AddressApi

    init {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-adresse.data.gouv.fr/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(AddressApi::class.java)
    }

    suspend fun getCitySuggestions(query: String): List<CitySuggestion> {
        return try {
            val response = api.searchCities(query)
            response.features.map { feature ->
                CitySuggestion(
                    label = feature.properties.label,
                    city = feature.properties.city,
                    postcode = feature.properties.postcode,
                    context = feature.properties.context,
                    latitude = feature.geometry.coordinates[1],
                    longitude = feature.geometry.coordinates[0]
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class CitySuggestion(
    val label: String,
    val city: String,
    val postcode: String,
    val context: String,
    val latitude: Double,
    val longitude: Double
)
