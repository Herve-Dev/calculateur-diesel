package com.example.dieselcalculateur.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AddressApi {
    @GET("search/")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("type") type: String = "municipality",
        @Query("limit") limit: Int = 5
    ): CityResponseDto
}
