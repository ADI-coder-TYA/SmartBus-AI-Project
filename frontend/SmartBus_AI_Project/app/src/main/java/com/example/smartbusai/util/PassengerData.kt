package com.example.smartbusai.util

import com.example.smartbusai.placesAPI.Location
import java.util.UUID

data class Passenger(
    var name: String = "",
    var age: String = "",
    var gender: String = "",
    var disability: String = "",
    var groupId : String? = null,
    val id: String = UUID.randomUUID().toString(),
    val seatNumber: String? = null,
    val pickupStopId: String = "",
    val dropStopId: String = "",
    val pickupLocation: Location? = null,
    val dropLocation: Location? = null
)
