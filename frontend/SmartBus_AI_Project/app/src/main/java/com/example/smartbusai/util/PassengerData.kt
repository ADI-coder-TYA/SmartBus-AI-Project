package com.example.smartbusai.util

import com.example.smartbusai.placesAPI.Location
import java.util.UUID
import kotlin.math.absoluteValue

// --- UI Model ---
data class Passenger(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int = 18,
    val gender: String = "Male",
    val disability: String = "None",
    val groupId: String? = null,
    val seatNumber: String? = null,

    // UI uses String IDs (Google Places)
    val pickupStopId: String? = null,
    val dropStopId: String? = null,

    val pickupLocation: Location? = null,
    val dropLocation: Location? = null
) {
    /**
     * Converts UI Model to API Request Model.
     */
    fun toApiRequest(pnr: String): PassengerApiRequest {
        return PassengerApiRequest(
            id = this.id,
            name = this.name.ifBlank { "Unknown" },
            age = this.age,
            gender = this.gender,
            disability = this.disability,
            groupId = this.groupId,

            // CRITICAL FIX: Python API demands Integers.
            // We convert the String ID to a stable Integer hash.
            pickupStopId = this.pickupStopId?.hashCode()?.absoluteValue ?: 1,
            dropStopId = this.dropStopId?.hashCode()?.absoluteValue ?: 10
        )
    }
}
