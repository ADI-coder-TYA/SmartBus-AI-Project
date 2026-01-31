package com.example.smartbusai.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbusai.BackendAPIConnector.RetrofitClient
import com.example.smartbusai.util.BookingHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor() : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingHistoryItem>>(emptyList())
    val bookings: StateFlow<List<BookingHistoryItem>> = _bookings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // The booking selected by the user to view details
    private val _selectedBooking = mutableStateOf<BookingHistoryItem?>(null)
    val selectedBooking: State<BookingHistoryItem?> = _selectedBooking

    fun selectBooking(booking: BookingHistoryItem) {
        _selectedBooking.value = booking
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.getAllBookings()
                if (response.isSuccessful && response.body() != null) {
                    _bookings.value = response.body()!!
                } else {
                    Log.e("SmartBus", "Failed to fetch bookings: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SmartBus", "Network error fetching bookings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
