package com.example.streamease.helper

import retrofit2.Call
import retrofit2.http.GET

interface Api  {
    @GET("Seats")
    fun getSeats(
    ): Call<String>


}