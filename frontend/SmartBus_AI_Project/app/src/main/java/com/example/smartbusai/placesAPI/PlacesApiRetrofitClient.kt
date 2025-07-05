package com.example.smartbusai.placesAPI

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PlacesApiRetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val api: GooglePlacesApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GooglePlacesApi::class.java)
}
