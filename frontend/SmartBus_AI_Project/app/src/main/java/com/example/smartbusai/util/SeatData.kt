package com.example.smartbusai.util

data class Seat(
    val row: Int,
    val col: Int,
    val seatNumber: String,
    val isAvailable: Boolean = true,
    val isReserved: Boolean = false,
    val type: SeatType = SeatType.REGULAR
)

enum class SeatType {
    REGULAR,
    WINDOW,
    AISLE,
    ACCESSIBLE
}

data class VehicleLayout(
    val rows: Int,
    val cols: Int,
    val seats: List<Seat>,
    val vehicleType: String
)
