package com.example.smartbusai.util

import com.google.gson.annotations.SerializedName

// --- Request Models (Strictly aligned with api.py) ---

data class SeatRequest(
    // Python expects "tripId" (camelCase), NOT "trip_id"
    val tripId: String?,

    // Python expects "vehicle" (camelCase), NOT "vehicle_config"
    val vehicle: VehicleReq,

    // Python expects "passengers"
    val passengers: List<PassengerApiRequest>,

// Python expects "route" (Optional)
val route: Map<String, Any> = emptyMap()
)

data class VehicleReq(
    val rows: Int,

    // Python expects "columns", but we use "cols" in Kotlin
    @SerializedName("columns") val cols: Int,

    // Python expects "vehicleType"
    val vehicleType: String = "AC Seater"
)

data class PassengerApiRequest(
    // Python expects "id", NOT "passenger_id"
    val id: String,

    val name: String?,
    val age: Int,
    val gender: String,
    val disability: String,

    // Python expects "groupId" (camelCase), NOT "group_id"
    val groupId: String?,

    // Python strictly requires Integers for stops
    val pickupStopId: Int,
    val dropStopId: Int
)

data class FeedbackRequest(
    val passengerId: String,
    val rating: Int,
    @SerializedName("seatLabel") val seatLabel: String,
    val totalRows: Int,
    val totalCols: Int
)

// --- Response Models ---

data class AllocationResponse(
    val tripId: String,
    val assignments: List<SeatAssignment>
)

data class SeatAssignment(
    // Python response maps assignments by passenger ID
    val passengerId: String,
    val seatId: String,
    val groupDistance: Double?,
    val seatType: String?,
    val explanation: String?
) {
    // Helper: Map 'seatId' (e.g., "1A") to 'seatLabel' for UI
    val seatLabel: String
        get() = seatId
}
