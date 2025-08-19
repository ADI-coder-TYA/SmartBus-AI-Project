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

    private val _query = mutableStateOf("")
    val query: State<String> = _query

    private val _results = MutableStateFlow<List<Prediction>>(emptyList())
    val results: StateFlow<List<Prediction>> = _results

    var selectedDeparture = mutableStateOf<PlaceDetails?>(null)
        private set

    var selectedDestination = mutableStateOf("")
        private set

    private val _placeLatLngMap = MutableStateFlow<Map<String, Location>>(emptyMap())
    val placeLatLngMap: StateFlow<Map<String, Location>> = _placeLatLngMap

    private val intermediateStopIds = mutableListOf<String>()
    private val departureStopId = mutableStateOf<String?>(null)
    private val destinationStopId = mutableStateOf<String?>(null)
    val intermediateStops = mutableStateListOf<String>()

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun updateDeparture(description: String, placeId: String) {
        selectedDeparture.value = PlaceDetails(description, placeId)
        departureStopId.value = placeId
    }

    fun updateDestination(newDestination: String) {
        selectedDestination.value = newDestination
    }

    fun updateDestinationStopId(newId: String) {
        destinationStopId.value = newId
    }

    fun updateIntermediateStop(index: Int, newText: String) {
        if (index in intermediateStops.indices) {
            intermediateStops[index] = newText
        }
    }

    fun updateIntermediateStopPlaceId(index: Int, newPlaceId: String) {
        if (index in intermediateStopIds.indices) {
            intermediateStopIds[index] = newPlaceId
        }
    }

    fun addIntermediateStop() {
        intermediateStops.add("")
        intermediateStopIds.add("")
    }

    fun removeIntermediateStop(index: Int) {
        if (index in intermediateStops.indices) {
            intermediateStops.removeAt(index)
            if (index in intermediateStopIds.indices) {
                intermediateStopIds.removeAt(index)
            }
        }
    }

    fun searchPlaces(query: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = PlacesApiRetrofitClient.api.getAutocomplete(query, apiKey)
                if (response.status == "OK") {
                    _results.value = response.predictions
                } else {
                    Log.d("SearchViewModel", "Error: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed", e)
            }
        }
    }

    private suspend fun getLatLngFromPlaceId(placeId: String): Location {
        val response = PlacesApiRetrofitClient.api.getPlaceDetails(
            placeId = placeId,
            apiKey = Constants.PLACES_API_KEY
        )

        val location = response.result?.geometry?.location
            ?: throw IllegalStateException("No geometry found for placeId: $placeId")

        return Location(location.lat, location.lng)
    }

    fun fetchLatLngFromPlaceId() {
        viewModelScope.launch {
            try {
                val deferredResults = mutableListOf<Deferred<Pair<String, Location>>>()

                departureStopId.value?.let { depId ->
                    deferredResults.add(async { depId to getLatLngFromPlaceId(depId) })
                }

                intermediateStopIds.forEach { stopId ->
                    deferredResults.add(async { stopId to getLatLngFromPlaceId(stopId) })
                }

                destinationStopId.value?.let { destId ->
                    deferredResults.add(async { destId to getLatLngFromPlaceId(destId) })
                }

                _placeLatLngMap.value = deferredResults.awaitAll().toMap()

            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error fetching coordinates", e)
            }
        }
    }
}
