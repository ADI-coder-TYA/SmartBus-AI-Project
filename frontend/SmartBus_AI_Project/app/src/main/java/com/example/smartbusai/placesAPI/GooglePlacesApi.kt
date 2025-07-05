package com.example.smartbusai.placesAPI

import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    @GET("place/autocomplete/json")
    suspend fun getAutocomplete(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("types") types: String = "geocode",
        @Query("components") components: String = "country:in"
    ): GooglePlacesResponse
}