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
                val seatNum = "${r + 1}${('A' + c)}"
                seats.add(
                    Seat(
                        row = r,
                        col = c,
                        seatNumber = seatNum,
                        type = when {
                            c == 0 || c == cols - 1 -> SeatType.WINDOW
                            else -> SeatType.AISLE
                        }
                    )
                )
            }
        }
        _layout.value = VehicleLayout(rows, cols, seats, type)
    }
}
