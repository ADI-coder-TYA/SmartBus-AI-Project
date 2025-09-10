package com.example.smartbusai.util

import com.example.smartbusai.placesAPI.Location
import java.util.UUID

data class Passenger(
    val id: String=UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int =18,
    val gender: String = "",
    val disability: String="",
    val groupId: String? = null,
    val pickupStopId: Int=1,
    val dropStopId: Int=2,
    val seatNumber: String? = null,

//    var name: String = "",
//    var age: String = "",
//    var gender: String = "",
//    var disability: String = "",
//    var groupId : String? = null,
//    val id: String = UUID.randomUUID().toString(),
//    val seatNumber: String? = null,
//    val pickupStopId: String = "",
//    val dropStopId: String = "",
    val pickupLocation: Location? = null,
    val dropLocation: Location? = null
)
