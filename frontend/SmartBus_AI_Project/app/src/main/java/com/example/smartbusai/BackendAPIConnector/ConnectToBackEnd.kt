package com.example.smartbusai.BackendAPIConnector

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.smartbusai.util.PassengerApiRequest
import com.example.smartbusai.util.SeatRequest
import com.example.smartbusai.util.VehicleConfig
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel
import com.example.smartbusai.viewmodels.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

fun onProceedButtonPressed(
    context: Context,
    navController: NavController,
    searchViewModel: SearchViewModel,
    passengerViewModel: PassengerViewModel,
    layoutViewModel: LayoutViewModel
) {
    // 1) Validation: Ensure Stops are selected
    val selectedDep = searchViewModel.selectedDeparture.value
    val selectedDest = searchViewModel.selectedDestination.value

    if (selectedDep == null || selectedDest.isEmpty()) {
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

    Toast.makeText(context, "Requesting AI Seat Allocation...", Toast.LENGTH_SHORT).show()

    // 4) Prepare Data for API
    // We map the UI 'PassengerData' to the API's 'PassengerApiRequest'
    val apiPassengers = uiPassengers.map { uiPassenger ->
        PassengerApiRequest(
            id = UUID.randomUUID().toString().substring(0, 8), // Generate a temp ID
            pnr = "PNR-${System.currentTimeMillis()}",
            name = uiPassenger.name,
            age = uiPassenger.age ,
            gender = uiPassenger.gender,
            groupId = "GRP-001" // Assumption: All booked together are one group
        )
    }

    val request = SeatRequest(
        tripId = "TRIP-${System.currentTimeMillis()}",
        // The AI model only needs rows/cols, not the full Vehicle object
        vehicleConfig = VehicleConfig(rows = layout.rows, cols = layout.cols),
        passengers = apiPassengers
    )

    // 5) Network Call (Using Coroutines)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // This is the new suspend call
            val response = RetrofitClient.api.allocateSeats(request)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // Log the results
                    Log.d("SmartBus", "Allocated ${body.assignments.size} seats for Trip ${body.tripId}")

                    // Show success
                    Toast.makeText(context, "AI Allocation Successful!", Toast.LENGTH_SHORT).show()

                    // Optional: You can update the passengerViewModel here with the new seat numbers
                    // passengerViewModel.updateSeats(body.assignments)

                    // Navigate to the next screen
                    navController.navigate("feedback")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown Server Error"
                    Toast.makeText(context, "Server Error: $errorMsg", Toast.LENGTH_LONG).show()
                    Log.e("SmartBus", "Server Error: $errorMsg")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Network Failure: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("SmartBus", "Network Exception", e)
            }
        }
    }
}
