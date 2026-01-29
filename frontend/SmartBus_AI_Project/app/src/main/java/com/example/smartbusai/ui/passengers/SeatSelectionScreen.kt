package com.example.smartbusai.ui.passengers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbusai.util.Passenger
import com.example.smartbusai.util.VehicleLayout
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel

// --- Screen 1: Vehicle Configuration ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTypeScreen(
    navController: NavController,
    layoutViewModel: LayoutViewModel,
    passengerViewModel: PassengerViewModel
) {
    // State for inputs
    var selectedType by remember { mutableStateOf("Bus") }
    var rowsInput by remember { mutableStateOf("10") }
    var colsInput by remember { mutableStateOf("4") }

    // Observables
    val allocationStatus by passengerViewModel.allocationStatus.collectAsState()
    val navigateNext by passengerViewModel.navigateToNext.collectAsState()

    // Navigation Side Effect
    LaunchedEffect(navigateNext) {
        if (navigateNext) {
            passengerViewModel.onNavigationHandled()
            navController.navigate("seat_visualizer")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Layout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0B1D39)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Define the vehicle structure to optimize seat allocation.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Vehicle Type Selection
            Text("Vehicle Type", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleTypeChip("Bus", selectedType == "Bus") { selectedType = "Bus" }
                VehicleTypeChip("Train", selectedType == "Train") { selectedType = "Train" }
            }

            Divider()

            // Dimensions Inputs
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = rowsInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) rowsInput = it },
                    label = { Text("Rows") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = colsInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) colsInput = it },
                    label = { Text("Columns") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.weight(1f))

            // Error Display
            if (allocationStatus != null && allocationStatus!!.startsWith("Error")) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            allocationStatus ?: "Unknown Error",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Proceed Button
            Button(
                onClick = {
                    val r = rowsInput.toIntOrNull() ?: 10
                    val c = colsInput.toIntOrNull() ?: 4

                    // 1. Create Layout
                    layoutViewModel.setLayout(r, c, selectedType)

                    // 2. Get Layout Object (It's synchronous in VM)
                    val layout = VehicleLayout(
                        r,
                        c,
                        emptyList(),
                        selectedType
                    ) // Temp obj to pass dimensions

                    // 3. Trigger AI
                    passengerViewModel.allocateSeats(layout)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B1D39)),
                enabled = allocationStatus != "Allocating..."
            ) {
                if (allocationStatus == "Allocating...") {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("AI Optimizing...")
                } else {
                    Icon(Icons.Default.DirectionsBus, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate & Allocate")
                }
            }
        }
    }
}

// --- Screen 2: Seat Visualization ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatLayoutScreen(
    navController: NavController,
    layoutViewModel: LayoutViewModel,
    passengerViewModel: PassengerViewModel
) {
    val layout by layoutViewModel.layout.collectAsState()
    val passengers by passengerViewModel.passengers.collectAsState()

    // Local state for the detail dialog
    var selectedPassenger by remember { mutableStateOf<Passenger?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seat Assignments", fontWeight = FontWeight.Bold)
                        Text(
                            "AI Optimized • ${passengers.size} Passengers",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF6F8FA))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(Color(0xFFE0E0E0), "Empty")
                Spacer(Modifier.width(16.dp))
                LegendItem(Color(0xFF4CAF50), "Occupied")
                Spacer(Modifier.width(16.dp))
                LegendItem(Color(0xFF2196F3), "Group")
            }

            // Grid Container
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (layout != null) {
                        SeatGrid(
                            layout = layout!!,
                            passengers = passengers,
                            onSeatClick = { seatLabel ->
                                // Find passenger in this seat
                                selectedPassenger = passengers.find { it.seatNumber == seatLabel }
                            }
                        )
                    } else {
                        Text("No Layout Defined")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* Finish Flow / Navigate Home */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B1D39))
            ) {
                Text("Confirm & Book")
            }
        }
    }

    // Passenger Detail Dialog
    if (selectedPassenger != null) {
        AlertDialog(
            onDismissRequest = { selectedPassenger = null },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50)) },
            title = { Text("Passenger Details") },
            text = {
                Column {
                    Text("Name: ${selectedPassenger!!.name}", fontWeight = FontWeight.Bold)
                    Text("Seat: ${selectedPassenger!!.seatNumber}")
                    Text("Age: ${selectedPassenger!!.age}")
                    Text("Gender: ${selectedPassenger!!.gender}")
                    if (selectedPassenger!!.groupId != "GRP-001") {
                        Text(
                            "Group ID: ${selectedPassenger!!.groupId?.take(6)}",
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPassenger = null }) { Text("Close") }
            }
        )
    }
}

// --- Components ---

@Composable
fun VehicleTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFE3F2FD),
            selectedLabelColor = Color(0xFF0B1D39),
            selectedLeadingIconColor = Color(0xFF0B1D39)
        )
    )
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(12.dp)
            .background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SeatGrid(
    layout: VehicleLayout,
    passengers: List<Passenger>,
    onSeatClick: (String) -> Unit
) {
    // Dynamic Grid Calculation
    LazyVerticalGrid(
        columns = GridCells.Fixed(layout.cols + 1), // +1 for the aisle gap if needed, or simple logic
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(layout.rows * layout.cols) { index ->
            val row = index / layout.cols
            val col = index % layout.cols

            // Reconstruct Label (Matches Backend Logic: 1A, 1B, 1C...)
            val label = "${row + 1}${('A' + col)}"

            // Check occupancy
            val occupant = passengers.find { it.seatNumber == label }
            val isOccupied = occupant != null
            val isGroup = occupant?.groupId != "GRP-001" && occupant?.groupId != null

            // Color Logic
            val seatColor = when {
                isGroup -> Color(0xFF2196F3) // Blue for groups
                isOccupied -> Color(0xFF4CAF50) // Green for individuals
                else -> Color(0xFFE0E0E0) // Gray for empty
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(seatColor, RoundedCornerShape(8.dp))
                    .clickable { if (isOccupied) onSeatClick(label) }
                    .border(
                        BorderStroke(1.dp, if (isOccupied) Color.Transparent else Color.LightGray),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AirlineSeatReclineNormal,
                        contentDescription = null,
                        tint = if (isOccupied) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isOccupied) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
