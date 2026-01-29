package com.example.smartbusai.BackendAPIConnector

import com.example.smartbusai.util.AllocationResponse
import com.example.smartbusai.util.SeatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {
    // This points to the Node.js endpoint: app.post('/allocate', ...)
    @POST("allocate")
    suspend fun allocateSeats(@Body request: SeatRequest): Response<AllocationResponse>
}
