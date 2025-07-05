package com.example.smartbusai.placesAPI

data class GooglePlacesResponse(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val place_id: String
)