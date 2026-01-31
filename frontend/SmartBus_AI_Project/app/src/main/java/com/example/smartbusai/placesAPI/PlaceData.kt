package com.example.smartbusai.placesAPI

import com.google.gson.annotations.SerializedName

data class GooglePlacesResponse(
    val predictions: List<Prediction>,
    val status: String,
    @SerializedName("error_message") val errorMessage: String? = null
)

data class Prediction(
    val description: String,
    val place_id: String,
    var lat: Double? = null,
    var lng: Double? = null
)

data class PlaceDetailsResponse(
    val result: PlaceResult,
    val status: String
)

data class PlaceResult(
    val geometry: Geometry
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
