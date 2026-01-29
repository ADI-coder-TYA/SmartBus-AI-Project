package com.example.smartbusai.util

import com.google.gson.annotations.SerializedName

// --- Request Models ---

data class SeatRequest(
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("vehicle_config") val vehicleConfig: VehicleConfig,
    @SerializedName("passengers") val passengers: List<PassengerApiRequest>
)

data class VehicleConfig(
    val rows: Int = 10,
    val cols: Int = 4,
    @SerializedName("aisle_col") val aisleCol: Int = 2,
    @SerializedName("seat_type_map") val seatTypeMap: Map<String, String> = emptyMap(),
    @SerializedName("reserved_seats") val reservedSeats: List<String> = emptyList()
)

data class PassengerApiRequest(
    val id: String,
    val pnr: String,
    val name: String,
    val age: Int,
    val gender: String, // "Male" or "Female"
    @SerializedName("group_id") val groupId: String
)

// --- Response Models ---

data class AllocationResponse(
    @SerializedName("trip_id") val tripId: String,
    val assignments: List<SeatAssignment>
)

data class SeatAssignment(
    @SerializedName("passenger_id") val passengerId: String,
    @SerializedName("seat_label") val seatLabel: String,
    val row: Int,
    val col: Int
)
