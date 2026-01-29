package com.example.smartbusai.viewmodels

import androidx.lifecycle.ViewModel
import com.example.smartbusai.util.Seat
import com.example.smartbusai.util.SeatType
import com.example.smartbusai.util.VehicleLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LayoutViewModel @Inject constructor() : ViewModel() {
    private val _layout = MutableStateFlow<VehicleLayout?>(null)
    val layout: StateFlow<VehicleLayout?> = _layout

    fun setLayout(rows: Int, cols: Int, type: String) {
        val seats = mutableListOf<Seat>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Generate labels: 1A, 1B, 1C, 1D...
                // This matches typical backend/AI standard
                val seatNum = "${r + 1}${('A' + c)}"

                // Determine Seat Type logic
                val seatType = when {
                    c == 0 || c == cols - 1 -> SeatType.WINDOW
                    // If it's a 5-seater row, the middle one (index 2) is regular
                    cols == 5 && c == 2 -> SeatType.REGULAR
                    else -> SeatType.AISLE
                }

                seats.add(
                    Seat(
                        row = r,
                        col = c,
                        seatNumber = seatNum,
                        type = seatType,
                        isAvailable = true // Default to true
                    )
                )
            }
        }
        _layout.value = VehicleLayout(rows, cols, seats, type)
    }

    /**
     * Helper to find a specific seat object by its label (e.g., "1A")
     * usage: layoutViewModel.getSeatByLabel("1A")
     */
    fun getSeatByLabel(label: String): Seat? {
        return _layout.value?.seats?.find { it.seatNumber == label }
    }
}
