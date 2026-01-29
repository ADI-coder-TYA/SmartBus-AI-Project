package com.example.smartbusai.ui.passengers

import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// Define Enum (Required for ViewModel)
enum class InputMode { NONE, MANUAL, CSV }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerSelectionScreen(
    searchViewModel: SearchViewModel,
    passengerViewModel: PassengerViewModel,
    navController: NavController
) {
    // --- Rich Theme Palette ---
    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val deepGreen = Color(0xFF008800)
    val surfaceColor = Color.White
    val bgColor = Color(0xFFF6F8FA)
    val mutedText = Color(0xFF6E6E6E)

    // --- State ---
    val inputMode by passengerViewModel.inputMode.collectAsState()
    val passengers by passengerViewModel.passengers.collectAsState()
    val isFinished by passengerViewModel.isFinished.collectAsState()
    val stops by searchViewModel.placeLatLngMap.collectAsState()

    // API Status monitoring
    val apiStatus by passengerViewModel.allocationStatus.collectAsState()
    val navigateNext by passengerViewModel.navigateToNext.collectAsState()

    // --- Navigation Side-Effect ---
    LaunchedEffect(navigateNext) {
        if (navigateNext) {
            passengerViewModel.onNavigationHandled()
            // Make sure this route matches your NavHost configuration
            navController.navigate("seat_selection_screen")
        }
    }

    // Local State
    var currentIndex by remember { mutableIntStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }

    val total = passengers.size
    val currentPassenger = passengers.getOrNull(currentIndex)

    Scaffold(
        containerColor = bgColor,
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
                                inputMode == InputMode.NONE -> "Select Input Method"
                                inputMode == InputMode.MANUAL && !isFinished -> "Passenger ${currentIndex + 1} of $total"
                                isFinished -> "Review & Group"
                                else -> "Import Data"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = navyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        bottomBar = {
            // Only show bottom bar actions if we are NOT in the initial selection mode
            if (inputMode != InputMode.NONE) {
                Surface(shadowElevation = 12.dp, color = surfaceColor) {
                    Column(Modifier.padding(16.dp)) {
                        // Helpful Tip Row
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (stops.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Loading route info...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = mutedText
                                    )
                                }
                            } else {
                                Text(
                                    "${stops.size} stops available",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = deepGreen
                                )
                            }

                            TextButton(onClick = { showHelp = !showHelp }) {
                                Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (showHelp) "Hide Tips" else "Show Tips",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Action Buttons Row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    passengerViewModel.reset()
                                    currentIndex = 0 // Reset local index too
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset / Cancel", color = navyBlue)
                            }

                            // Navigation Logic for Manual Mode
                            if (inputMode == InputMode.MANUAL && !isFinished && total > 0) {
                                Button(
                                    onClick = {
                                        if (currentIndex < total - 1) {
                                            currentIndex++
                                        } else {
                                            passengerViewModel.markFinished()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = navyBlue)
                                ) {
                                    Text(if (currentIndex < total - 1) "Next Passenger" else "Review Summary")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Help Card
                if (showHelp) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("💡 Quick Tips", fontWeight = FontWeight.Bold, color = navyBlue)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "• Select pickup/drop stops from the dropdowns.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• Use CSV import for large groups (Format: Name, Age, Gender, Disability).",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• You can group passengers in the Summary screen to ensure they sit together.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Main Content Switching
                when {
                    // 1. Initial Selection Screen
                    inputMode == InputMode.NONE -> {
                        Spacer(Modifier.height(32.dp))
                        SelectionCard(
                            title = "Manual Entry",
                            desc = "Add passengers one by one. Good for small families.",
                            icon = Icons.Default.Edit,
                            onClick = { passengerViewModel.setInputMode(InputMode.MANUAL) }
                        )
                        SelectionCard(
                            title = "Import CSV",
                            desc = "Upload a list from a file. Best for large groups.",
                            icon = Icons.Default.List,
                            onClick = { passengerViewModel.setInputMode(InputMode.CSV) }
                        )
                    }

                    // 2. CSV Import Mode
                    inputMode == InputMode.CSV && !isFinished -> {
                        CSVFilePickerScreen(onParsed = { list ->
                            passengerViewModel.setPassengersFromCsv(
                                list
                            )
                        }, onCancel = { passengerViewModel.reset() })
                    }

                    // 3. Manual Entry Form
                    inputMode == InputMode.MANUAL && !isFinished -> {
                        if (passengers.isEmpty()) {
                            // First time asking for count
                            NumberDialog(onConfirm = { count ->
                                passengerViewModel.initPassengers(
                                    count
                                )
                            }, onCancel = { passengerViewModel.reset() })
                        } else {
                            // The actual form
                            currentPassenger?.let { p ->
                                PassengerForm(
                                    passenger = p,
                                    index = currentIndex,
                                    total = total,
                                    stops = stops,
                                    onUpdate = { updated ->
                                        passengerViewModel.updatePassenger(
                                            currentIndex,
                                            updated
                                        )
                                    },
                                    onNext = {
                                        if (currentIndex < total - 1) {
                                            currentIndex++
                                        } else {
                                            passengerViewModel.markFinished()
                                        }
                                    },
                                    onBack = {
                                        if (currentIndex > 0) currentIndex-- else passengerViewModel.reset()
                                    }
                                )
                            }
                        }
                    }

                    // 4. Summary & Grouping
                    isFinished -> {
                        SummaryScreen(
                            passengers = passengers,
                            onUpdatePassenger = { i, p ->
                                passengerViewModel.updatePassenger(
                                    i,
                                    p
                                )
                            },
                            onSeatLayoutSelect = {
                                // Navigate to layout selection logic
                                navController.navigate("layout_selection_screen")
                            }
                        )
                    }
                }
            }

            // Loading Overlay for API calls
            if (apiStatus == "Allocating...") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("AI is optimizing seats...")
                        }
                    }
                }
            }
        }
    }
}

// --- Components ---

@Composable
fun SelectionCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color(0xFF0B1D39))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun NumberDialog(onConfirm: (Int) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Default.Person, null) },
        title = { Text("How many passengers?") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                label = { Text("Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.toIntOrNull() ?: 1) },
                enabled = text.isNotEmpty() && (text.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B1D39))
            ) { Text("Start Entry") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerForm(
    passenger: Passenger,
    index: Int,
    total: Int,
    stops: Map<String, Location>,
    onUpdate: (Passenger) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    // Form State (Sync with prop)
    var name by remember(passenger) { mutableStateOf(passenger.name) }
    var age by remember(passenger) { mutableStateOf(passenger.age.toString()) }
    var gender by remember(passenger) { mutableStateOf(passenger.gender) }
    var disability by remember(passenger) { mutableStateOf(passenger.disability) }

    // Dropdowns
    var genderExpanded by remember { mutableStateOf(false) }
    var disabilityExpanded by remember { mutableStateOf(false) }
    var pickupExpanded by remember { mutableStateOf(false) }
    var dropExpanded by remember { mutableStateOf(false) }

    val stopNames = stops.keys.toList()

    fun updateModel() {
        onUpdate(
            passenger.copy(
                name = name,
                age = age.toIntOrNull() ?: 18,
                gender = gender,
                disability = disability
            )
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFC107), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Passenger Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            // Inputs
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; updateModel() },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            age = it; updateModel()
                        }
                    },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Gender Dropdown
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        listOf("Male", "Female", "Other").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = { gender = item; updateModel(); genderExpanded = false })
                        }
                    }
                }
            }

            // Disability Dropdown
            ExposedDropdownMenuBox(
                expanded = disabilityExpanded,
                onExpandedChange = { disabilityExpanded = !disabilityExpanded }
            ) {
                OutlinedTextField(
                    value = disability.ifEmpty { "None" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Disability / Special Needs") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = disabilityExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Favorite, null) }
                )
                ExposedDropdownMenu(
                    expanded = disabilityExpanded,
                    onDismissRequest = { disabilityExpanded = false }) {
                    listOf(
                        "None",
                        "Wheelchair",
                        "Visual Impairment",
                        "Hearing Impairment",
                        "Other"
                    ).forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                disability = item; updateModel(); disabilityExpanded = false
                            })
                    }
                }
            }

            // Stops (If available)
            if (stopNames.isNotEmpty()) {
                Text(
                    "Route Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF0B1D39)
                )

                // Pickup
                ExposedDropdownMenuBox(
                    expanded = pickupExpanded,
                    onExpandedChange = { pickupExpanded = !pickupExpanded }) {
                    // FIX: Safe access to pickupStopId which now exists in Passenger
                    val currentPickup = passenger.pickupStopId ?: "Select Pickup"

                    OutlinedTextField(
                        value = currentPickup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pickup") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pickupExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8F5E9),
                            unfocusedContainerColor = Color(0xFFF1F8E9)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = pickupExpanded,
                        onDismissRequest = { pickupExpanded = false }) {
                        stopNames.forEach { stop ->
                            DropdownMenuItem(
                                text = { Text(stop) },
                                onClick = {
                                    // FIX: Update using the new fields
                                    onUpdate(
                                        passenger.copy(
                                            pickupStopId = stop,
                                            pickupLocation = stops[stop]
                                        )
                                    )
                                    pickupExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Drop
                ExposedDropdownMenuBox(
                    expanded = dropExpanded,
                    onExpandedChange = { dropExpanded = !dropExpanded }) {
                    // FIX: Safe access to dropStopId
                    val currentDrop = passenger.dropStopId ?: "Select Drop-off"

                    OutlinedTextField(
                        value = currentDrop,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Drop-off") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8F5E9),
                            unfocusedContainerColor = Color(0xFFF1F8E9)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dropExpanded,
                        onDismissRequest = { dropExpanded = false }) {
                        stopNames.forEach { stop ->
                            DropdownMenuItem(
                                text = { Text(stop) },
                                onClick = {
                                    // FIX: Update using the new fields
                                    onUpdate(
                                        passenger.copy(
                                            dropStopId = stop,
                                            dropLocation = stops[stop]
                                        )
                                    )
                                    dropExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Navigation Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Button(
                    onClick = { updateModel(); onNext() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008800))
                ) {
                    Text(if (index == total - 1) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
fun CSVFilePickerScreen(onParsed: (List<Passenger>) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var errorMsg by remember { mutableStateOf("") }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                try {
                    val list = readCsvFromUri(context, it)
                    if (list.isNotEmpty()) onParsed(list) else errorMsg =
                        "File is empty or invalid format."
                } catch (e: Exception) {
                    errorMsg = "Error: ${e.localizedMessage}"
                }
            }
        }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.DateRange,
            null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF0B1D39)
        )
        Spacer(Modifier.height(16.dp))
        Text("Import Passenger List", style = MaterialTheme.typography.headlineSmall)
        Text("Supports .csv files", color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { launcher.launch(arrayOf("text/csv", "text/comma-separated-values")) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B1D39))
        ) {
            Text("Select File")
        }
        TextButton(onClick = onCancel) { Text("Cancel") }

        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun readCsvFromUri(context: Context, uri: Uri): List<Passenger> {
    val passengers = mutableListOf<Passenger>()
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.lineSequence().forEachIndexed { index, line ->
                // Skip header if it exists
                if (index == 0 && line.lowercase().contains("name")) return@forEachIndexed

                val parts = line.split(",").map { it.trim() }
                if (parts.size >= 4) {
                    passengers.add(
                        Passenger(
                            id = UUID.randomUUID().toString(),
                            name = parts[0],
                            age = parts[1].toIntOrNull() ?: 18,
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
    val selectedIndices = remember { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Review Passengers",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF0B1D39)
            )
            Text(
                "Select passengers to group them together (AI will try to seat them nearby).",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(passengers) { index, p ->
                    val isSelected = selectedIndices.contains(index)
                    val isGrouped = !p.groupId.isNullOrEmpty() && p.groupId != "GRP-001"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedIndices.remove(index) else selectedIndices.add(
                                    index
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFF2196F3)
                        ) else null
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${p.gender}, ${p.age} years",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (p.pickupStopId != null) {
                                    Text(
                                        "From: ${p.pickupStopId} To: ${p.dropStopId ?: "?"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }

                                if (isGrouped) {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            "Grouped",
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 2.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Group Action
            if (selectedIndices.size > 1) {
                OutlinedButton(
                    onClick = {
                        val newGroup = UUID.randomUUID().toString().take(8)
                        selectedIndices.forEach { i ->
                            onUpdatePassenger(i, passengers[i].copy(groupId = newGroup))
                        }
                        selectedIndices.clear()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Group with Selected (${selectedIndices.size})")
                }
            }

            // Proceed Action
            Button(
                onClick = onSeatLayoutSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B1D39))
            ) {
                Text(
                    "Proceed to Seat Selection",
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}