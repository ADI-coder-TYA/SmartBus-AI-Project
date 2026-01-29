package com.example.smartbusai.util

import com.example.smartbusai.placesAPI.Location
import java.util.UUID

// --- UI Model ---
data class Passenger(
    val id: String = UUID.randomUUID().toString(), // Unique ID generated on creation
    val name: String = "",
    val age: Int = 18,
    val gender: String = "Male", // Default to avoid empty strings
    val disability: String = "None",
    val groupId: String? = null,
    val seatNumber: String? = null,
    val pickupLocation: Location? = null,
    val dropLocation: Location? = null,
    val pickupStopId: String? = null, // To store the selected stop name/ID
    val dropStopId: String? = null,   // To store the selected stop name/ID
) {
    /**
     * Converts UI Model to API Request Model.
     * We use the first 8 chars of the UUID to satisfy the backend's preference for short IDs.
     */
    fun toApiRequest(pnr: String): PassengerApiRequest {
        return PassengerApiRequest(
            id = this.id.take(8),
            pnr = pnr,
            name = this.name,
            age = this.age,
            gender = this.gender,
            groupId = this.groupId ?: "GRP-DEFAULT"
        )
    }
}