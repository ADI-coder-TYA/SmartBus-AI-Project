package com.example.streamease.helper

import com.example.smartbusai.util.SeatRequest
import com.example.smartbusai.util.SeatResponse
import com.example.smartbusai.util.HealthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    // Health check endpoint (GET /health)
    @GET("health")
    fun getHealth(): Call<HealthResponse>

    // Seating allocation endpoint (POST /allocate)
    @POST("allocate")
    fun allocateSeats(@Body request: SeatRequest): Call<SeatResponse>
}
