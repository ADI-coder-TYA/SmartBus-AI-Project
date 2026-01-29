package com.example.smartbusai.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbusai.BackendAPIConnector.RetrofitClient
import com.example.smartbusai.ui.passengers.InputMode
import com.example.smartbusai.util.FeedbackRequest
import com.example.smartbusai.util.Passenger
import com.example.smartbusai.util.SeatAssignment
import com.example.smartbusai.util.SeatRequest
import com.example.smartbusai.util.VehicleConfig
import com.example.smartbusai.util.VehicleLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PassengerViewModel @Inject constructor() : ViewModel() {

    // --- State ---

    // List of passengers
    private val _passengers = MutableStateFlow<List<Passenger>>(emptyList())
    val passengers: StateFlow<List<Passenger>> = _passengers

    // Input mode (Manual vs CSV)
    private val _inputMode = MutableStateFlow(InputMode.NONE)
    val inputMode: StateFlow<InputMode> = _inputMode

    // Track if user is done entering details and viewing Summary
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    // API Status
    private val _allocationStatus = MutableStateFlow<String?>(null)
    val allocationStatus: StateFlow<String?> = _allocationStatus

    // Navigation Trigger
    private val _navigateToNext = MutableStateFlow(false)
    val navigateToNext: StateFlow<Boolean> = _navigateToNext

    // --- Actions ---

    fun setInputMode(mode: InputMode) {
        _inputMode.value = mode
        // Reset finished state if we change modes, unless we are just viewing
        if (mode == InputMode.NONE) _isFinished.value = false
    }

    fun initPassengers(count: Int) {
        if (count > 0) {
            _passengers.value = List(count) { Passenger() }
            _isFinished.value = false
        }
    }

    fun updatePassenger(index: Int, passenger: Passenger) {
        val currentList = _passengers.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = passenger
            _passengers.value = currentList
        }
    }

    // Called when CSV is parsed
    fun setPassengersFromCsv(list: List<Passenger>) {
        _passengers.value = list
        _isFinished.value = true // Jump straight to summary
    }

    // Called when manual entry loop is done
    fun markFinished() {
        _isFinished.value = true
    }

    // Reset everything (e.g. Cancel button)
    fun reset() {
        _passengers.value = emptyList()
        _inputMode.value = InputMode.NONE
        _isFinished.value = false
        _allocationStatus.value = null
    }

    // --- Backend Logic ---

    fun allocateSeats(layout: VehicleLayout) {
        viewModelScope.launch {
            _allocationStatus.value = "Allocating..."

            try {
                val pnr = "PNR-${System.currentTimeMillis()}"
                val currentPassengers = _passengers.value

                // Convert UI model to API model
                val apiPassengers = currentPassengers.map { it.toApiRequest(pnr) }

                val request = SeatRequest(
                    tripId = "TRIP-${System.currentTimeMillis()}",
                    vehicleConfig = VehicleConfig(rows = layout.rows, cols = layout.cols),
                    passengers = apiPassengers
                )

                val response = RetrofitClient.api.allocateSeats(request)

                if (response.isSuccessful && response.body() != null) {
                    val assignments = response.body()!!.assignments
                    updatePassengerSeats(assignments)
                    _allocationStatus.value = "Success"
                    _navigateToNext.value = true
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown Server Error"
                    _allocationStatus.value = "Error: $errorMsg"
                    Log.e("SmartBus", "Allocation Failed: $errorMsg")
                }

            } catch (e: Exception) {
                _allocationStatus.value = "Network Error: ${e.message}"
                Log.e("SmartBus", "Exception", e)
            }
        }
    }

    private fun updatePassengerSeats(assignments: List<SeatAssignment>) {
        val currentList = _passengers.value.toMutableList()
        val updatedList = currentList.map { passenger ->
            // Match using ID (assuming ID was sent correctly)
            val assignment = assignments.find {
                it.passengerId == passenger.id || it.passengerId == passenger.id.take(8)
            }
            if (assignment != null) {
                passenger.copy(seatNumber = assignment.seatLabel)
            } else {
                passenger
            }
        }
        _passengers.value = updatedList
    }

    fun onNavigationHandled() {
        _navigateToNext.value = false
        _allocationStatus.value = null
    }

    fun submitFeedback(rating: Int, rows: Int, cols: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPassengers = _passengers.value

            currentPassengers.forEach { passenger ->
                if (passenger.seatNumber != null) {
                    try {
                        val req = FeedbackRequest(
                            passengerId = passenger.id, // The ID we generated earlier
                            rating = rating,
                            seatLabel = passenger.seatNumber,
                            totalRows = rows,
                            totalCols = cols
                        )
                        RetrofitClient.api.submitFeedback(req)
                        Log.d("SmartBus", "Feedback sent for ${passenger.name}")
                    } catch (e: Exception) {
                        Log.e("SmartBus", "Failed to send feedback", e)
                    }
                }
            }
            // Navigate home after sending
            withContext(Dispatchers.Main) {
                reset() // Clear data for next booking
                // navigation handled by UI observing a state, or callback
            }
        }
    }
}
