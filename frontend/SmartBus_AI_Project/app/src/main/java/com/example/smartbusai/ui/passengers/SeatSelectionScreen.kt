package com.example.smartbusai.ui.passengers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbusai.util.Seat
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel


@Composable
fun VehicleTypeScreen(
    layoutViewModel: LayoutViewModel,
    onProceed: () -> Unit
) {
    var selectedType by remember { mutableStateOf("Bus") }
    var rows by remember { mutableStateOf("10") }
    var cols by remember { mutableStateOf("4") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Vehicle Type", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Bus", "Train").forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }

        OutlinedTextField(
            value = rows,
            onValueChange = { rows = it },
            label = { Text("Rows") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cols,
            onValueChange = { cols = it },
            label = { Text("Columns") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                layoutViewModel.setLayout(rows.toInt(), cols.toInt(), selectedType)
                onProceed()
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Proceed", color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}


@Composable
fun SeatLayoutScreen(
    layoutViewModel: LayoutViewModel,
    passengerViewModel: PassengerViewModel,
    onConfirm: () -> Unit
) {
    val layout by layoutViewModel.layout.collectAsState()
    val passengers by passengerViewModel.passengers.collectAsState()
    var selectedSeat by remember { mutableStateOf<Seat?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colorScheme.background)) {
        Text(
            "Select Seats (${layout?.vehicleType})",
            style = MaterialTheme.typography.headlineSmall
        )

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
                                onClick = { selectedSeat = seat },
                                colors = ButtonDefaults.buttonColors(
                                    if (seat.isReserved) Color.Gray else MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Text(seat.seatNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onConfirm, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("Confirm Seats", color = MaterialTheme.colorScheme.onSecondary)
        }
    }

    // Dialog for passenger assignment
    if (selectedSeat != null) {
        AlertDialog(
            onDismissRequest = { selectedSeat = null },
            title = { Text("Assign Passenger") },
            text = {
                Column {
                    passengers.forEach { p ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    passengerViewModel.assignSeat(p.id, selectedSeat!!.seatNumber)
                                    selectedSeat = null
                                }
                                .padding(8.dp)
                        ) {
                            Text("${p.name} (${p.age}, ${p.gender})")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
