package com.example.dieselcalculateur.data.remote

import retrofit2.http.GET

interface UpdateApi {
    @GET("repos/Herve-Dev/calculateur-diesel/releases/latest")
    suspend fun getLatestRelease(): GitHubReleaseDto
}
