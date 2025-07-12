package com.example.smartbusai.placesAPI

data class GooglePlacesResponse(
    val predictions: List<Prediction>,
    val status: String
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
