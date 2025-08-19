package com.example.smartbusai.ui.passengers

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smartbusai.viewmodels.PassengerViewModel
import com.example.smartbusai.viewmodels.SearchViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

data class Passenger(
    var name: String = "",
    var age: String = "",
    var gender: String = "",
    var disability: String = ""
)

enum class InputMode { NONE, MANUAL, CSV }

@Composable
fun PassengerSelectionScreen(
    searchViewModel: SearchViewModel,
    passengerViewModel: PassengerViewModel,
    onFinished: () -> Unit = {} // navigate to SeatAllotmentScreen
) {
    val inputMode by passengerViewModel.inputMode.collectAsState()
    val passengers by passengerViewModel.passengers.collectAsState()
    val isFinished by passengerViewModel.isFinished.collectAsState()

    // Local UI tracker for which passenger is being edited
    var currentIndex by remember { mutableStateOf(0) }

    when (inputMode) {
        InputMode.NONE -> {
            ModeSelectionDialog(
                onManual = { passengerViewModel.setInputMode(InputMode.MANUAL) },
                onCsv = { passengerViewModel.setInputMode(InputMode.CSV) }
            )
        }

        InputMode.MANUAL -> {
            if (passengers.isEmpty() && !isFinished) {
                NumberDialog(
                    onConfirm = { count -> passengerViewModel.initPassengers(count) }
                )
            } else if (isFinished) {
                SummaryScreen(passengers)
                // Move forward to ML model
                LaunchedEffect(Unit) { onFinished() }
            } else {
                PassengerForm(
                    passenger = passengers[currentIndex],
                    index = currentIndex,
                    total = passengers.size,
                    onNext = {
                        if (currentIndex < passengers.size - 1) {
                            currentIndex++
                        } else {
                            passengerViewModel.markFinished()
                        }
                    },
                    onBack = {
                        if (currentIndex > 0) currentIndex--
                    },
                    onUpdate = { updated ->
                        passengerViewModel.updatePassenger(currentIndex, updated)
                    }
                )
            }
        }

        InputMode.CSV -> {
            CSVFilePickerScreen(
                onParsed = { parsed -> passengerViewModel.setPassengersFromCsv(parsed) }
            )
            if (isFinished) {
                SummaryScreen(passengers)
                LaunchedEffect(Unit) { onFinished() }
            }
        }
    }
}

@Composable
fun ModeSelectionDialog(onManual: () -> Unit, onCsv: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Select Input Method") },
        text = { Text("Do you want to enter passengers manually or from a CSV file?") },
        confirmButton = { TextButton(onClick = onManual) { Text("Manual Entry") } },
        dismissButton = { TextButton(onClick = onCsv) { Text("CSV File") } }
    )
}

@Composable
fun NumberDialog(onConfirm: (Int) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Number of Passengers") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) input = it },
                label = { Text("Enter count") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val count = input.toIntOrNull() ?: 0
                if (count > 0) onConfirm(count)
            }) { Text("OK") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerForm(
    passenger: Passenger,
    index: Int,
    total: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onUpdate: (Passenger) -> Unit
) {
    var name by remember { mutableStateOf(passenger.name) }
    var age by remember { mutableStateOf(passenger.age) }
    var gender by remember { mutableStateOf(passenger.gender) }
    var disability by remember { mutableStateOf(passenger.disability) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Passenger ${index + 1} of $total", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; onUpdate(passenger.copy(name = it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it; onUpdate(passenger.copy(age = it)) },
            label = { Text("Age") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Gender dropdown
        var expandedGender by remember { mutableStateOf(false) }
        val genders = listOf("Male", "Female", "Other")

        ExposedDropdownMenuBox(
            expanded = expandedGender,
            onExpandedChange = { expandedGender = !expandedGender } // toggle properly
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // 👈 important!
            )
            ExposedDropdownMenu(
                expanded = expandedGender,
                onDismissRequest = { expandedGender = false }
            ) {
                genders.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            gender = it
                            onUpdate(passenger.copy(gender = it))
                            expandedGender = false
                        }
                    )
                }
            }
        }

// Disability dropdown
        var expandedDisability by remember { mutableStateOf(false) }
        val disabilities =
            listOf("None", "Wheelchair", "Visual Impairment", "Hearing Impairment", "Other")

        ExposedDropdownMenuBox(
            expanded = expandedDisability,
            onExpandedChange = { expandedDisability = !expandedDisability } // toggle properly
        ) {
            OutlinedTextField(
                value = disability,
                onValueChange = {},
                readOnly = true,
                label = { Text("Disability") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDisability) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // 👈 important!
            )
            ExposedDropdownMenu(
                expanded = expandedDisability,
                onDismissRequest = { expandedDisability = false }
            ) {
                disabilities.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            disability = it
                            onUpdate(passenger.copy(disability = it))
                            expandedDisability = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            if (index > 0) OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onNext) { Text(if (index == total - 1) "Finish" else "Next") }
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
fun SummaryScreen(passengers: List<Passenger>) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Passenger Summary", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        passengers.forEachIndexed { i, p ->
            Text("Passenger ${i + 1}: ${p.name}, Age ${p.age}, ${p.gender}, Disability: ${p.disability}")
        }
    }
}
