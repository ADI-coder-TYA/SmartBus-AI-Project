package com.example.smartbusai.BackendAPIConnector

import android.content.Context
import android.widget.Toast
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel
import com.example.smartbusai.viewmodels.SearchViewModel

fun onProceedButtonPressed(
    context: Context,
    searchViewModel: SearchViewModel,
    passengerViewModel: PassengerViewModel,
    layoutViewModel: LayoutViewModel
) {
    // 1) Validation: Ensure Stops are selected
    val selectedDep = searchViewModel.selectedDeparture.value
    val selectedDest = searchViewModel.selectedDestination.value

    if (selectedDep == null || selectedDest?.placeId?.isEmpty() == true) {
        Toast.makeText(context, "Please select departure and destination", Toast.LENGTH_LONG).show()
        return
    }

    // 2) Validation: Layout
    val layout = layoutViewModel.layout.value
    if (layout == null) {
        Toast.makeText(context, "Please choose vehicle layout first", Toast.LENGTH_LONG).show()
        return
    }

    // 3) Validation: Passengers
    val uiPassengers = passengerViewModel.passengers.value
    if (uiPassengers.isEmpty()) {
        Toast.makeText(context, "Please add passengers first", Toast.LENGTH_LONG).show()
        return
    }

    // 4) Validation: Ensure passenger details are filled (optional but recommended)
    val invalidPassenger = uiPassengers.any { it.name.isBlank() }
    if (invalidPassenger) {
        Toast.makeText(context, "Please fill in all passenger names", Toast.LENGTH_SHORT).show()
        return
    }

    Toast.makeText(context, "Requesting AI Seat Allocation...", Toast.LENGTH_SHORT).show()

    // 5) Trigger ViewModel Action
    // We pass the layout configuration so the VM knows rows/cols
    passengerViewModel.allocateSeats(layout)
}
