package com.example.smartbusai.BackendAPIConnector

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.smartbusai.util.Route
import com.example.smartbusai.util.SeatRequest
import com.example.smartbusai.util.SeatResponse
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel
import com.example.smartbusai.viewmodels.SearchViewModel
import com.example.streamease.helper.RetrofitClient

fun onProceedButtonPressed(
    context: Context,
    navController: NavController,
    searchViewModel: SearchViewModel,
    passengerViewModel: PassengerViewModel,
    layoutViewModel: LayoutViewModel
) {
    // 1) Collect route (departure/destination) locations
    val placeMap = searchViewModel.placeLatLngMap.value           // Map<placeId, Location>
    val selectedDep = searchViewModel.selectedDeparture.value     // PlaceDetails? (expect placeId inside)
    val selectedDest = searchViewModel.selectedDestination.value  // String

    if (selectedDep == null || selectedDest.isEmpty()) {
        Toast.makeText(context, "Please select departure and destination", Toast.LENGTH_LONG).show()
        return
    }

    // Resolve actual Location objects for departure & destination
    val depLoc = placeMap[selectedDep.placeId]
    val destLoc = placeMap[searchViewModel.destinationStopId.value]

    if (depLoc == null || destLoc == null) {
        Toast.makeText(context, "Waiting for stop coordinates to be available. Try again in a moment.", Toast.LENGTH_LONG).show()
        // Optionally trigger a fetch:
        searchViewModel.fetchLatLngFromPlaceId()
        return
    }

    // 2) Layout
    val layout = layoutViewModel.layout.value
    if (layout == null) {
        Toast.makeText(context, "Please choose vehicle layout first", Toast.LENGTH_LONG).show()
        return
    }

    // 3) Passengers
    val uiPassengers = passengerViewModel.passengers.value
    if (uiPassengers.isEmpty()) {
        Toast.makeText(context, "Please add passengers first", Toast.LENGTH_LONG).show()
        return
    }

    val seatRequest = SeatRequest(
        route = Route(departure = depLoc, destination = destLoc),
        passengers = uiPassengers,
        vehicleType = layout.vehicleType,
        rows = layout.rows,
        columns = layout.cols
    )

    val call = RetrofitClient.instance?.api?.getSeats(seatRequest)
    if (call == null) {
        Toast.makeText(context, "Network client not initialized", Toast.LENGTH_LONG).show()
        return
    }

    // Optional: show a quick Toast / spinner before request
    Toast.makeText(context, "Requesting seat assignments...", Toast.LENGTH_SHORT).show()

    call.enqueue(object : retrofit2.Callback<SeatResponse> {
        override fun onResponse(call: retrofit2.Call<SeatResponse>, response: retrofit2.Response<SeatResponse>) {
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    Toast.makeText(context, "Empty response from server", Toast.LENGTH_LONG).show()
                    return
                }

                // body.assignments: Map<String, SeatAssignment>
                // Keys likely are passenger ids. We'll apply seat numbers to passengers
                val assignments = body.assignments

                // Apply seat assignments to passengerViewModel
                assignments.forEach { (passengerId, seatAssignment) ->
                    val seatId = seatAssignment.seat_id
                    // Update passenger in viewmodel (we assume assignSeat exists)
                    passengerViewModel.assignSeat(passengerId, seatId)
                }

                Toast.makeText(context, "Seats assigned successfully", Toast.LENGTH_SHORT).show()

                // Navigate where you need (feedback or seat layout to show assigned seats)
                navController.navigate("feedback")
            } else {
                val code = response.code()
                val err = response.errorBody()?.string()
                Toast.makeText(context, "Server error: $code", Toast.LENGTH_LONG).show()
                Log.e("SeatCall", "Error $code, body=$err")
            }
        }

        override fun onFailure(call: retrofit2.Call<SeatResponse>, t: Throwable) {
            Toast.makeText(context, "Network failure: ${t.message}", Toast.LENGTH_LONG).show()
            Log.e("SeatCall", "Failure", t)
        }
    })
}
