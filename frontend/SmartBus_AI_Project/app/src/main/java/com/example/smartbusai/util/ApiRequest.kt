package com.example.smartbusai.util

import com.example.smartbusai.placesAPI.Location

data class SeatRequest(
    val route: Route,
    val vehicle: VehicleReq,
    val passengers: List<Passenger>,
    val tripId: String? = null
)

data class Route(
    val departure: Location,
    val destination: Location
)

data class VehicleReq(
    val rows: Int,
    val columns: Int,
    val vehicleType: String = "AC Seater"
)

//data class PassengerReq(
//    val id: String,
//    val name: String? = null,
//    val age: Int,
//    val gender: String,
//    val disability: String,
//    val groupId: String? = null,
//    val pickupStopId: Int,
//    val dropStopId: Int
//)

// Response models (as before)

data class HealthResponse(
    val status: String,
    val models: ModelsStatus
)

data class ModelsStatus(
    val seat_type: Boolean,
    val penalty: Boolean,
    val encoder: Boolean
)

data class SeatResponse(
    val assignments: Map<String, Assignment>
)

data class Assignment(
    val tripId: String,
    val passengerId: String,
    val groupId: String?,
    val seatId: String,
    val groupDistance: Float?,
    val seatType: String?,
    val normRow: Int?,
    val normCol: Int?,
    val explanation: String
)
