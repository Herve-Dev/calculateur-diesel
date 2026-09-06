package com.example.dieselcalculateur.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface FuelStationApi {
    @GET("stations/around/{lat},{lon}")
    suspend fun getStationsAround(
        @Path("lat") latitude: Double,
        @Path("lon") longitude: Double,
        @Query("responseFields") responseFields: String = "Fuels,Price,Brand,Address",
        @Header("Accept") accept: String = "application/json"
    ): List<FuelStationDto>
}
