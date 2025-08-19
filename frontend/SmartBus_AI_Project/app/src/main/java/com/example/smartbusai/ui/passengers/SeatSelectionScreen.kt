package com.example.smartbusai.ui.passengers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel

@Composable
fun SeatLayoutScreen(
    layoutViewModel: LayoutViewModel,
    passengerViewModel: PassengerViewModel,
    onConfirm: () -> Unit
) {
    val layout by layoutViewModel.layout.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select Your Seats", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        layout?.let { l ->
            LazyColumn {
                items(l.rows) { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (0 until l.cols).forEach { col ->
                            val seat = l.seats.first { it.row == row && it.col == col }
                            Button(
                                onClick = {
                                    // TODO: seat selection logic (assign to passenger later)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    if (seat.isReserved) Color.Gray else Color.Green
                                ),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Text(seat.seatNumber)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onConfirm, modifier = Modifier.align(Alignment.End)) {
            Text("Confirm Layout")
        }
    }
}
