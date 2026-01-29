package com.example.smartbusai.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbusai.ui.passengers.InputMode
import com.example.smartbusai.util.Passenger
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

    fun assignSeat(passengerId: String, seatNumber: String) {
        _passengers.value = _passengers.value.map {
            if (it.id == passengerId) it.copy(seatNumber = seatNumber) else it
        }
    }

    private val _allocationResult = MutableStateFlow<String?>(null)
    val allocationResult: StateFlow<String?> = _allocationResult

    fun allocateSeats(passengersFromUi: List<PassengerData>) {
        viewModelScope.launch {
            try {
                // 1. Convert UI Data to API Data
                val apiPassengers = passengersFromUi.map { uiPassenger ->
                    PassengerApiRequest(
                        id = UUID.randomUUID().toString().substring(0, 8), // Generate temp ID
                        pnr = "PNR-${System.currentTimeMillis()}",
                        name = uiPassenger.name,
                        age = uiPassenger.age.toIntOrNull() ?: 25, // Fallback if parse fails
                        gender = uiPassenger.gender,
                        groupId = "GRP-001" // Assume they are travelling together
                    )
                }

                // 2. Create the Request Payload
                // We hardcode a standard bus config for now
                val request = SeatRequest(
                    tripId = "TRIP-${System.currentTimeMillis()}",
                    vehicleConfig = VehicleConfig(rows = 10, cols = 4),
                    passengers = apiPassengers
                )

                // 3. Call the Backend
                val response = RetrofitClient.api.allocateSeats(request)

                if (response.isSuccessful && response.body() != null) {
                    val assignments = response.body()!!.assignments
                    // Process result (e.g., show a success message or navigate)
                    _allocationResult.value = "Allocated ${assignments.size} seats!"

                    assignments.forEach { seat ->
                        Log.d("SmartBus", "Passenger ${seat.passengerId} got seat ${seat.seatLabel}")
                    }
                } else {
                    _allocationResult.value = "Error: ${response.errorBody()?.string()}"
                }

            } catch (e: Exception) {
                Log.e("SmartBus", "Network Error", e)
                _allocationResult.value = "Network Error: ${e.localizedMessage}"
            }
        }
    }
}
