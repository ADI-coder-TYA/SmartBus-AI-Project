package com.example.smartbusai.ui.passengers

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartbusai.placesAPI.Location
import com.example.smartbusai.util.Passenger
import com.example.smartbusai.viewmodels.PassengerViewModel
import com.example.smartbusai.viewmodels.SearchViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

enum class InputMode { NONE, MANUAL, CSV }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerSelectionScreen(
    searchViewModel: SearchViewModel,
    passengerViewModel: PassengerViewModel,
    navController: NavController,
    onFinished: () -> Unit = {}
) {
    // Theme palette (consistent)
    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val deepGreen = Color(0xFF008800)
    val surface = Color.White
    val muted = Color(0xFF6E6E6E)

    // ViewModel state
    val inputMode by passengerViewModel.inputMode.collectAsState()
    val passengers by passengerViewModel.passengers.collectAsState()
    val isFinished by passengerViewModel.isFinished.collectAsState()
    val stops by searchViewModel.placeLatLngMap.collectAsState() // Map<String, Location>

    // local UI state
    var currentIndex by remember { mutableStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }

    // derived
    val total = passengers.size.takeIf { it > 0 } ?: 0
    val currentPassenger = passengers.getOrNull(currentIndex)

    Scaffold(
        containerColor = Color(0xFFF6F8FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Passengers",
                            style = MaterialTheme.typography.titleLarge,
                            color = navyBlue
                        )
                        Text(
                            text = when {
                                inputMode == InputMode.NONE -> "Choose input method"
                                inputMode == InputMode.MANUAL && total > 0 -> "Passenger ${currentIndex + 1} of $total"
                                inputMode == InputMode.MANUAL -> "Manual entry"
                                inputMode == InputMode.CSV -> "Import from CSV"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = muted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = navyBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surface)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp, color = surface) {
                Column {
                    // Helpful small status / progress row
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (stops.isEmpty()) {
                            // show spinner if stops haven't been fetched yet
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = deepGreen
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Fetching route coordinates...",
                                    color = muted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Text(
                                "Stops available: ${stops.size}",
                                color = muted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // small help / summary toggle
                        TextButton(onClick = { showHelp = !showHelp }) {
                            Icon(Icons.Default.Info, contentDescription = "Help", tint = navyBlue)
                            Spacer(Modifier.width(6.dp))
                            Text(if (showHelp) "Hide tips" else "Tips", color = navyBlue)
                        }
                    }

                    // Action buttons row
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                // save current edits (if any) and go back
                                currentPassenger?.let {
                                    passengerViewModel.updatePassenger(
                                        currentIndex,
                                        it
                                    )
                                }
                                navController.navigateUp()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = navyBlue)
                        }

                        Row {
                            TextButton(
                                onClick = {
                                    // quick jump to summary if some passengers exist
                                    if (passengers.isNotEmpty()) passengerViewModel.markFinished()
                                },
                                enabled = passengers.isNotEmpty(),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text("View summary", color = navyBlue)
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    // If finished, go to layout; otherwise go next passenger
                                    if (currentIndex < passengers.size - 1) {
                                        // ensure current is saved
                                        currentPassenger?.let {
                                            passengerViewModel.updatePassenger(
                                                currentIndex,
                                                it
                                            )
                                        }
                                        currentIndex++
                                    } else {
                                        // last passenger: mark finished -> shows summary
                                        passengerViewModel.markFinished()
                                        // navigate to layout will be handled from SummaryScreen button
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = deepGreen)
                            ) {
                                Text(
                                    if (currentIndex < passengers.size - 1) "Next" else "Finish",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Help / tips area (collapsible) ---
            if (showHelp) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Tips for passenger entry",
                            fontWeight = FontWeight.SemiBold,
                            color = navyBlue
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "- Select the pickup and drop stops for each passenger from the route stops.",
                            color = muted
                        )
                        Text("- You can import from CSV for many passengers faster.", color = muted)
                        Text(
                            "- Group related passengers on the summary screen for group cohesion.",
                            color = muted
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // --- Mode handling ---
            when (inputMode) {
                InputMode.NONE -> {
                    // show attractive prompt card instead of a blocking dialog
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(containerColor = surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Add passengers",
                                style = MaterialTheme.typography.titleMedium,
                                color = navyBlue
                            )
                            Text(
                                "You can add passengers manually or import a CSV file (Name,Age,Gender,Disability,Pickup,Drop).",
                                color = muted
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { passengerViewModel.setInputMode(InputMode.MANUAL) },
                                    colors = ButtonDefaults.buttonColors(containerColor = deepGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Manual", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { passengerViewModel.setInputMode(InputMode.CSV) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = navyBlue
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("CSV", color = navyBlue)
                                }
                            }
                        }
                    }
                }

                InputMode.MANUAL -> {
                    if (passengers.isEmpty() && !isFinished) {
                        NumberDialog(onConfirm = { count -> passengerViewModel.initPassengers(count) })
                    } else if (isFinished) {
                        // show polished summary when finished
                        SummaryScreen(
                            passengers = passengers,
                            onSeatLayoutSelect = {
                                navController.navigate("layout")
                            },
                            onUpdatePassenger = { idx, p ->
                                passengerViewModel.updatePassenger(
                                    idx,
                                    p
                                )
                            }
                        )
                    } else {
                        // Show the form for current passenger
                        currentPassenger?.let { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    PassengerForm(
                                        passenger = p,
                                        index = currentIndex,
                                        total = total,
                                        stops = stops,
                                        onNext = {
                                            // Save current and go next or finish
                                            passengerViewModel.updatePassenger(currentIndex, p)
                                            if (currentIndex < total - 1) currentIndex++ else passengerViewModel.markFinished()
                                        },
                                        onBack = {
                                            passengerViewModel.updatePassenger(currentIndex, p)
                                            if (currentIndex > 0) currentIndex-- else navController.navigateUp()
                                        },
                                        onUpdate = { updated ->
                                            passengerViewModel.updatePassenger(
                                                currentIndex,
                                                updated
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                InputMode.CSV -> {
                    // CSV import UI
                    CSVFilePickerScreen(onParsed = { parsed ->
                        passengerViewModel.setPassengersFromCsv(parsed)
                    })

                    if (isFinished) {
                        SummaryScreen(
                            passengers = passengers,
                            onSeatLayoutSelect = { navController.navigate("layout") },
                            onUpdatePassenger = { idx, p ->
                                passengerViewModel.updatePassenger(
                                    idx,
                                    p
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeSelectionDialog(onManual: () -> Unit, onCsv: () -> Unit) {
    // Brand colors (same palette we've been using)
    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val deepGreen = Color(0xFF008800)
    val surface = Color.White

    AlertDialog(
        onDismissRequest = { /* intentionally no-op to force a choice */ },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // small decorative icon matching theme
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(goldenYellow, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = "Mode",
                        tint = navyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Passenger Input",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = navyBlue
                    )
                    Text(
                        text = "Choose how you'll provide passenger data",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    "Enter passengers manually (one-by-one) or import a CSV file with columns: Name,Age,Gender,Disability,Pickup,Drop.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onManual,
                colors = ButtonDefaults.buttonColors(containerColor = deepGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Manual",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Manual Entry", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCsv,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = navyBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "CSV",
                    tint = navyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Import CSV")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun NumberDialog(onConfirm: (Int) -> Unit) {
    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val surface = Color.White

    var input by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* keep it modal until explicit action */ },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(goldenYellow, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Number",
                        tint = navyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Number of Passengers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = navyBlue
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        // keep only digits
                        if (it.all { ch -> ch.isDigit() } && it.length <= 3) {
                            input = it
                            showError = false
                        }
                    },
                    label = { Text("Enter count", color = Color.DarkGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please enter a number greater than 0.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tip: For CSV import, the first row may be a header. Fields expected: Name,Age,Gender,Disability,Pickup,Drop",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = input.toIntOrNull() ?: 0
                    if (count > 0) {
                        onConfirm(count)
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = navyBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("OK", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // clear and close: since original had no dismiss param, we just clear
                    input = ""
                    showError = false
                }
            ) {
                Text("Cancel", color = navyBlue)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = surface,
        tonalElevation = 6.dp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerForm(
    passenger: Passenger,
    index: Int,
    total: Int,
    stops: Map<String, Location>, // map: stopName -> Location
    onNext: () -> Unit,
    onBack: () -> Unit,
    onUpdate: (Passenger) -> Unit
) {
    // Theme colors (same palette)
    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val deepGreen = Color(0xFF008800)
    val grayText = Color(0xFF777777)

    // Keep local UI state and sync when `passenger` changes
    var name by remember { mutableStateOf(passenger.name) }
    var age by remember { mutableStateOf(passenger.age) }
    var gender by remember { mutableStateOf(passenger.gender) }
    var disability by remember { mutableStateOf(passenger.disability) }

    // pickup/drop choices derivation
    val stopNames = remember(stops) { stops.keys.toList() }
    var pickupName by remember { mutableStateOf(if (stopNames.isNotEmpty()) stopNames.first() else "") }
    var dropName by remember { mutableStateOf(if (stopNames.isNotEmpty()) stopNames.last() else "") }

    // set the Location objects (nullable until stops are available)
    val pickupLocation: Location? = stops[pickupName]
    val dropLocation: Location? = stops[dropName]

    // dropdown expanded states
    var expandedGender by remember { mutableStateOf(false) }
    var expandedDisability by remember { mutableStateOf(false) }
    var expandedPickup by remember { mutableStateOf(false) }
    var expandedDrop by remember { mutableStateOf(false) }

    // When the passenger object changes externally, sync local state
    LaunchedEffect(passenger) {
        name = passenger.name
        age = passenger.age
        gender = passenger.gender
        disability = passenger.disability

        // if passenger already had pickup/drop, prefer them (fallback to first/last stops)
        passenger.pickupStopId?.let { id ->
            if (stops.containsKey(id)) pickupName = id
        }
        passenger.dropStopId?.let { id ->
            if (stops.containsKey(id)) dropName = id
        }
    }

    // helper to build an updated passenger and call onUpdate
    fun saveLocalToModel() {
        val updated = passenger.copy(
            name = name,
            age = age,
            gender = gender,
            disability = disability,
            pickupLocation = pickupLocation,
            dropLocation = dropLocation,
            pickupStopId = pickupName,
            dropStopId = dropName
        )
        onUpdate(updated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Passenger ${index + 1} of $total",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = navyBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Provide passenger details and pick-up / drop-off stops",
                    style = MaterialTheme.typography.bodySmall.copy(color = grayText)
                )
            }
            // small avatar placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(goldenYellow),
                contentAlignment = Alignment.Center
            ) {
                Text((index + 1).toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                // optimistic update
                onUpdate(passenger.copy(name = it))
            },
            label = { Text("Full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors().copy(
                //focusedBorderColor = navyBlue,
                cursorColor = navyBlue
            )
        )

        // Age
        OutlinedTextField(
            value = age,
            onValueChange = {
                if (it.all { ch -> ch.isDigit() } && it.length <= 3) {
                    age = it
                    onUpdate(passenger.copy(age = it))
                }
            },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors().copy(
                //focusedBorderColor = navyBlue,
                cursorColor = navyBlue
            )
        )

        // Gender
        ExposedDropdownMenuBox(
            expanded = expandedGender,
            onExpandedChange = { expandedGender = !expandedGender }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = TextFieldDefaults.colors().copy(
                    //focusedBorderColor = navyBlue
                )
            )
            ExposedDropdownMenu(
                expanded = expandedGender,
                onDismissRequest = { expandedGender = false }
            ) {
                val genders = listOf("Male", "Female", "Other")
                genders.forEach { g ->
                    DropdownMenuItem(
                        text = { Text(g) },
                        onClick = {
                            gender = g
                            onUpdate(passenger.copy(gender = g))
                            expandedGender = false
                        }
                    )
                }
            }
        }

        // Disability
        ExposedDropdownMenuBox(
            expanded = expandedDisability,
            onExpandedChange = { expandedDisability = !expandedDisability }
        ) {
            OutlinedTextField(
                value = disability,
                onValueChange = {},
                readOnly = true,
                label = { Text("Disability (if any)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDisability) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = TextFieldDefaults.colors().copy(
                    //focusedBorderColor = navyBlue
                )
            )
            ExposedDropdownMenu(
                expanded = expandedDisability,
                onDismissRequest = { expandedDisability = false }
            ) {
                val disabilities =
                    listOf("None", "Wheelchair", "Visual Impairment", "Hearing Impairment", "Other")
                disabilities.forEach { d ->
                    DropdownMenuItem(
                        text = { Text(d) },
                        onClick = {
                            disability = d
                            onUpdate(passenger.copy(disability = d))
                            expandedDisability = false
                        }
                    )
                }
            }
        }

        // Pickup / Drop section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Pickup & Drop-off",
                    style = MaterialTheme.typography.titleSmall.copy(color = navyBlue)
                )

                if (stopNames.isEmpty()) {
                    // no stops — friendly message
                    Text(
                        "No route stops available. Please go back to Route Selection and choose departure/route so stops can be fetched.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                    )
                } else {
                    // Pickup
                    ExposedDropdownMenuBox(
                        expanded = expandedPickup,
                        onExpandedChange = { expandedPickup = !expandedPickup }
                    ) {
                        OutlinedTextField(
                            value = pickupName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pickup stop") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPickup) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = TextFieldDefaults.colors().copy(
                                //focusedBorderColor = deepGreen
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPickup,
                            onDismissRequest = { expandedPickup = false }
                        ) {
                            stopNames.forEach { nameKey ->
                                DropdownMenuItem(
                                    text = { Text(nameKey) },
                                    onClick = {
                                        pickupName = nameKey
                                        onUpdate(
                                            passenger.copy(
                                                pickupStopId = nameKey,
                                                pickupLocation = stops[nameKey]
                                            )
                                        )
                                        expandedPickup = false
                                    }
                                )
                            }
                        }
                    }

                    // Drop
                    ExposedDropdownMenuBox(
                        expanded = expandedDrop,
                        onExpandedChange = { expandedDrop = !expandedDrop }
                    ) {
                        OutlinedTextField(
                            value = dropName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Drop-off stop") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDrop) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = TextFieldDefaults.colors().copy(
                                //focusedBorderColor = deepGreen
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDrop,
                            onDismissRequest = { expandedDrop = false }
                        ) {
                            stopNames.forEach { nameKey ->
                                DropdownMenuItem(
                                    text = { Text(nameKey) },
                                    onClick = {
                                        dropName = nameKey
                                        onUpdate(
                                            passenger.copy(
                                                dropStopId = nameKey,
                                                dropLocation = stops[nameKey]
                                            )
                                        )
                                        expandedDrop = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Navigation buttons: Back and Next (Next disabled if required fields missing)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    // Save and go back
                    saveLocalToModel()
                    onBack()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back")
            }

            val isValid = name.isNotBlank() && age.isNotBlank() && stopNames.isNotEmpty()
            Button(
                onClick = {
                    // save and next
                    saveLocalToModel()
                    onNext()
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = deepGreen)
            ) {
                Text(if (index == total - 1) "Finish" else "Next", color = Color.White)
            }
        }
    }
}

@Composable
fun CSVFilePickerScreen(onParsed: (List<Passenger>) -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf("") }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                try {
                    val passengers = readCsvFromUri(context, it)
                    onParsed(passengers)
                } catch (e: Exception) {
                    error = "Failed to read CSV: ${e.message}"
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Select a CSV file with format: Name,Age,Gender,Disability",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = {
            launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
        }) {
            Text("Pick CSV File")
        }
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun readCsvFromUri(context: Context, uri: android.net.Uri): List<Passenger> {
    val passengers = mutableListOf<Passenger>()
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            val lines = reader.readLines()

            if (lines.isEmpty()) return passengers

            val firstRow = lines.first().split(",").map { it.trim().lowercase() }
            val isHeader = firstRow.containsAll(listOf("name", "age", "gender", "disability"))

            val dataLines = if (isHeader) lines.drop(1) else lines

            dataLines.forEach { line ->
                val parts = line.split(",").map { it.trim() }
                if (parts.size >= 4) {
                    passengers.add(
                        Passenger(
                            name = parts[0],
                            age = parts[1],
                            gender = parts[2],
                            disability = parts[3]
                        )
                    )
                }
            }
        }
    }
    return passengers
}

@Composable
fun SummaryScreen(
    passengers: List<Passenger>,
    onUpdatePassenger: (Int, Passenger) -> Unit,
    onSeatLayoutSelect: () -> Unit
) {
    val selected = remember { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Text(
            "Passenger Summary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // Passenger list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(passengers) { index, p ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected.contains(index))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selected.contains(index)) selected.remove(index)
                            else selected.add(index)
                        }
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${p.name}, Age ${p.age}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Gender: ${p.gender}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Disability: ${p.disability}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Pickup: ${p.pickupLocation}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Drop-off: ${p.dropLocation}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (p.groupId?.isNotEmpty() ?: false) {
                                Text(
                                    "Group: ${((p.groupId?.take(6)) ?: "")}…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Checkbox(
                            checked = selected.contains(index),
                            onCheckedChange = {
                                if (it) selected.add(index) else selected.remove(index)
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Group creation button
        Button(
            onClick = {
                if (selected.isNotEmpty()) {
                    val groupId = UUID.randomUUID().toString()
                    selected.forEach { idx ->
                        val p = passengers[idx]
                        onUpdatePassenger(idx, p.copy(groupId = groupId))
                    }
                    selected.clear()
                }
            },
            enabled = selected.size >= 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Create Group")
        }

        Spacer(Modifier.height(12.dp))

        // Proceed button
        Button(
            onClick = onSeatLayoutSelect,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Proceed to Seat Layout Selection")
        }
    }
}
