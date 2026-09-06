package com.example.dieselcalculateur.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FuelStationApi {
    @GET("records")
    suspend fun getStations(
        @Query("where") where: String? = "prix_gazole is not null",
        @Query("limit") limit: Int = 100,
        @Query("geofilter.distance") geofilterDistance: String? = null
    ): FuelStationResponse
}
