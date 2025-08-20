package com.example.streamease.helper

import com.example.smartbusai.util.SeatRequest
import com.example.smartbusai.util.SeatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api  {
    @POST("Seats")
    fun getSeats(@Body request: SeatRequest): Call<SeatResponse>


}