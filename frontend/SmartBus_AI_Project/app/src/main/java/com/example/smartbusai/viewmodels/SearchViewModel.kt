package com.example.smartbusai.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbusai.placesAPI.PlacesApiRetrofitClient
import com.example.smartbusai.placesAPI.Prediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(): ViewModel() {

    private val _query = mutableStateOf("")
    val query: State<String> = _query

    private val _results = MutableStateFlow<List<Prediction>>(emptyList())
    val results: StateFlow<List<Prediction>> = _results

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun searchPlaces(query: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = PlacesApiRetrofitClient.api.getAutocomplete(query, apiKey)
                if (response.status == "OK") {
                    _results.value = response.predictions
                } else {
                    Log.d("SearchViewModel", "Error fetching places: ${response.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
