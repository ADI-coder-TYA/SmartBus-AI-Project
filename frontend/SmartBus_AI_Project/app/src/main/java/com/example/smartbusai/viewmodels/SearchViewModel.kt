package com.example.smartbusai.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbusai.constants.Constants
import com.example.smartbusai.placesAPI.Location
import com.example.smartbusai.placesAPI.PlacesApiRetrofitClient
import com.example.smartbusai.placesAPI.Prediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceDetails(
    val description: String,
    val placeId: String
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- API Results ---
    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())
    val predictions: StateFlow<List<Prediction>> = _predictions

    // --- Departure State ---
    private val _departureText = mutableStateOf("")
    val departureText: State<String> = _departureText

    private val _selectedDeparture = mutableStateOf<PlaceDetails?>(null)
    val selectedDeparture: State<PlaceDetails?> = _selectedDeparture

    // --- Destination State ---
    private val _destinationText = mutableStateOf("")
    val destinationText: State<String> = _destinationText

    private val _selectedDestination = mutableStateOf<PlaceDetails?>(null)
    val selectedDestination: State<PlaceDetails?> = _selectedDestination

    // --- Intermediate Stops State ---
    // UI Text for each stop
    val intermediateStopsText = mutableStateListOf<String>()

    // Selected Objects for each stop (nullable if not selected yet)
    val intermediateStopsDetails = mutableListOf<PlaceDetails?>()

    // --- Coordinate Map (For final routing) ---
    private val _placeLatLngMap = MutableStateFlow<Map<String, Location>>(emptyMap())
    val placeLatLngMap: StateFlow<Map<String, Location>> = _placeLatLngMap

    // ---------------------------------------------------------
    // Actions
    // ---------------------------------------------------------

    // -- Departure --
    fun onDepartureQueryChange(newText: String) {
        _departureText.value = newText
        // If user is typing, invalidate previous selection until they pick again
        if (_selectedDeparture.value?.description != newText) {
            _selectedDeparture.value = null
        }
        fetchPredictions(newText)
    }

    fun onDepartureSelected(prediction: Prediction) {
        _departureText.value = prediction.description
        _selectedDeparture.value = PlaceDetails(prediction.description, prediction.place_id)
        clearPredictions()
    }

    // -- Destination --
    fun onDestinationQueryChange(newText: String) {
        _destinationText.value = newText
        if (_selectedDestination.value?.description != newText) {
            _selectedDestination.value = null
        }
        fetchPredictions(newText)
    }

    fun onDestinationSelected(prediction: Prediction) {
        _destinationText.value = prediction.description
        _selectedDestination.value = PlaceDetails(prediction.description, prediction.place_id)
        clearPredictions()
    }

    // -- Intermediate Stops --
    fun addIntermediateStop() {
        intermediateStopsText.add("")
        intermediateStopsDetails.add(null)
    }

    fun removeIntermediateStop(index: Int) {
        if (index in intermediateStopsText.indices) {
            intermediateStopsText.removeAt(index)
            intermediateStopsDetails.removeAt(index)
        }
    }

    fun onIntermediateQueryChange(index: Int, newText: String) {
        if (index in intermediateStopsText.indices) {
            intermediateStopsText[index] = newText
            intermediateStopsDetails[index] = null // Invalidate selection while typing
            fetchPredictions(newText)
        }
    }

    fun onIntermediateStopSelected(index: Int, prediction: Prediction) {
        if (index in intermediateStopsText.indices) {
            intermediateStopsText[index] = prediction.description
            intermediateStopsDetails[index] =
                PlaceDetails(prediction.description, prediction.place_id)
            clearPredictions()
        }
    }

    // ---------------------------------------------------------
    // API Logic
    // ---------------------------------------------------------

    private fun fetchPredictions(query: String) {
        viewModelScope.launch {
            if (query.length < 3) {
                _predictions.value = emptyList()
                return@launch
            }
            try {
                val response =
                    PlacesApiRetrofitClient.api.getAutocomplete(query, Constants.PLACES_API_KEY)
                if (response.status == "OK") {
                    _predictions.value = response.predictions
                    Log.i("SearchViewModel", "Search Results: ${_predictions.value}")
                } else {
                    _predictions.value = emptyList()
                    Log.e("SearchViewModel", "Response Error: ${response.status}, ${response.errorMessage}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search Error", e)
                _predictions.value = emptyList()
            }
        }
    }

    private fun clearPredictions() {
        _predictions.value = emptyList()
    }

    // ---------------------------------------------------------
    // Coordinate Resolution (Called before navigation)
    // ---------------------------------------------------------
    fun fetchLatLngFromPlaceId() {
        viewModelScope.launch {
            try {
                val deferredResults = mutableListOf<Deferred<Pair<String, Location>>>()

                // Departure
                _selectedDeparture.value?.let { place ->
                    deferredResults.add(async { place.placeId to getLatLngFromPlaceId(place.placeId) })
                }

                // Intermediate
                intermediateStopsDetails.filterNotNull().forEach { place ->
                    deferredResults.add(async { place.placeId to getLatLngFromPlaceId(place.placeId) })
                }

                // Destination
                _selectedDestination.value?.let { place ->
                    deferredResults.add(async { place.placeId to getLatLngFromPlaceId(place.placeId) })
                }

                val results = deferredResults.awaitAll().toMap()
                _placeLatLngMap.value = results
                Log.d("SearchViewModel", "Fetched ${results.size} locations")

            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error fetching coordinates", e)
            }
        }
    }

    private suspend fun getLatLngFromPlaceId(placeId: String): Location {
        val response = PlacesApiRetrofitClient.api.getPlaceDetails(
            placeId = placeId,
            apiKey = Constants.PLACES_API_KEY
        )
        return response.result?.geometry?.location
            ?: throw IllegalStateException("No geometry for $placeId")
    }
}
