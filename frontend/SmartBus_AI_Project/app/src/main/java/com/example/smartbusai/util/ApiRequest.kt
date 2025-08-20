package com.example.smartbusai.util

import com.example.smartbusai.placesAPI.Location

data class SeatRequest(
    val route: Route,
    val passengers: List<Passenger>,
    val vehicleType: String,
    val rows: Int,
    val columns: Int
)

data class Route(
    val departure: Location,
    val destination: Location
)

data class SeatResponse(
    val assignments: Map<String, SeatAssignment>
)

data class SeatAssignment(
    val seat_id: String,
    val universal_features: UniversalFeatures
)

data class UniversalFeatures(
    val seat_type: String,
    val norm_row: Double,
    val norm_col: Double,
    val group_distance: Double? // nullable because in JSON it can be null
)
