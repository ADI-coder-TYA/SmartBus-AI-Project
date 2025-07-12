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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _query = mutableStateOf("")
    val query: State<String> = _query

    private val _results = MutableStateFlow<List<Prediction>>(emptyList())
    val results: StateFlow<List<Prediction>> = _results

    private val intermediateStopIds = mutableListOf<String>()
    private val departureStopId = mutableStateOf<String?>(null)
    private val destinationStopId = mutableStateOf<String?>(null)

    var selectedDeparture = mutableStateOf(savedStateHandle.get<String>("departureLocation"))
        private set

    var selectedDestination = mutableStateOf("")
        private set

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    var routeGeocodeList = mutableStateListOf<Location>()
        private set

    fun updateDestination(newDestination: String) {
        selectedDestination.value = newDestination
    }

    fun updateDeparture(newDeparture: String) {
        selectedDeparture.value = newDeparture
    }

    fun updateDepartureStopId(newId: String) {
        departureStopId.value = newId
    }

    fun updateDestinationStopId(newId: String) {
        destinationStopId.value = newId
    }

    val intermediateStops = mutableStateListOf<String>()

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
            Log.d("SearchViewModel", "Fetching places for query: $query")
            try {
                val response = PlacesApiRetrofitClient.api.getAutocomplete(query, apiKey)
                if (response.status == "OK") {
                    _results.value = response.predictions
                    Log.d("SearchViewModel", "Fetched ${response.predictions.size} places")
                } else {
                    Log.d("SearchViewModel", "Error fetching places: ${response.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchLatLngFromPlaceId() {
        viewModelScope.launch {
            try {
                val placeIdList =
                    intermediateStopIds + departureStopId.value!! + destinationStopId.value!!
                placeIdList.forEach { placeId ->
                    val response =
                        PlacesApiRetrofitClient.api.getPlaceDetails(
                            placeId,
                            Constants.PLACES_API_KEY
                        )
                    val location = response.result.geometry.location
                    val lat = location.lat
                    val lng = location.lng
                    Log.d("SearchViewModel", "Fetched lat: $lat, lng: $lng for placeId: $placeId")
                    routeGeocodeList.add(Location(lat = lat, lng = lng))
                }

            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to get lat/lng", e)
            }
        }
    }
}
