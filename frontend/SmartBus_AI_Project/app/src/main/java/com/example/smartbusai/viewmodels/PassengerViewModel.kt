package com.example.smartbusai.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbusai.ui.passengers.InputMode
import com.example.smartbusai.ui.passengers.Passenger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PassengerViewModel @Inject constructor() : ViewModel() {

    // Backing state for list of passengers
    private val _passengers = MutableStateFlow<List<Passenger>>(emptyList())
    val passengers: StateFlow<List<Passenger>> = _passengers

    // Track input mode (Manual, CSV, etc.)
    private val _inputMode = MutableStateFlow(InputMode.NONE)
    val inputMode: StateFlow<InputMode> = _inputMode

    // Track whether passenger entry is completed
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    // Set input mode (called from UI)
    fun setInputMode(mode: InputMode) {
        _inputMode.value = mode
    }

    // Initialize passenger list (Manual mode)
    fun initPassengers(count: Int) {
        if (count > 0) {
            _passengers.value = List(count) { Passenger() }
            _isFinished.value = false
        }
    }

    // Update a passenger at specific index
    fun updatePassenger(index: Int, passenger: Passenger) {
        val currentList = _passengers.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = passenger
            _passengers.value = currentList
        }
    }

    // Add passengers from CSV
    fun setPassengersFromCsv(list: List<Passenger>) {
        _passengers.value = list
        _isFinished.value = true
    }

    // Mark process as finished
    fun markFinished() {
        _isFinished.value = true
    }

    // Clear all passengers
    fun reset() {
        _passengers.value = emptyList()
        _inputMode.value = InputMode.NONE
        _isFinished.value = false
    }

    // Debug helper for logging
    fun debugPrintPassengers() {
        viewModelScope.launch {
            _passengers.value.forEachIndexed { index, passenger ->
                Log.d("PassengerViewModel", "Passenger $index: $passenger")
            }
        }
    }
}
