package com.example.smartbusai.util

// --- Booking History Models ---

data class BookingHistoryItem(
    val _id: String, // MongoDB ID
    val tripId: String,
    val vehicle: VehicleReq, // Reusing your existing VehicleReq
    val assignments: List<SeatAssignment>,
    val createdAt: String
)