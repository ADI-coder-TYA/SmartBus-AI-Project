package com.example.smartbusai.ui.route

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbusai.placesAPI.Prediction
import com.example.smartbusai.viewmodels.SearchViewModel

// --- Constants & Colors ---
private val NavyBlue = Color(0xFF0B1D39)
private val GoldenYellow = Color(0xFFFFC107)
private val DeepGreen = Color(0xFF008800)
private val LightGray = Color(0xFFF5F5F5)
private val BorderGray = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelectionScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val scrollState = rememberScrollState()

    // Read state to determine if we can proceed
    val selectedDep by searchViewModel.selectedDeparture
    val selectedDest by searchViewModel.selectedDestination
    val isReadyToProceed = selectedDep != null && selectedDest != null

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = NavyBlue
                ),
                title = {
                    Text("Plan Your Journey", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyBlue
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        searchViewModel.fetchLatLngFromPlaceId()
                        navController.navigate("passengerSelection")
                    },
                    enabled = isReadyToProceed,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyBlue,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Passengers", fontSize = 16.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Where are we going today?",
                style = MaterialTheme.typography.headlineSmall,
                color = NavyBlue,
                fontWeight = FontWeight.SemiBold
            )
            RouteFormCard(searchViewModel)
        }
    }
}

@Composable
fun RouteFormCard(viewModel: SearchViewModel) {
    val predictions by viewModel.predictions.collectAsState()
    val focusManager = LocalFocusManager.current

    // State Variables
    val departureText by viewModel.departureText
    val destinationText by viewModel.destinationText
    val intermediateStops = viewModel.intermediateStopsText

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- DEPARTURE ---
            TimelineInputItem(
                label = "Departure",
                text = departureText,
                onTextChange = { viewModel.onDepartureQueryChange(it) },
                predictions = predictions,
                isLast = false,
                iconColor = DeepGreen,
                onPredictionSelect = {
                    viewModel.onDepartureSelected(it)
                    focusManager.clearFocus()
                },
                onClear = { viewModel.onDepartureQueryChange("") }
            )

            // --- INTERMEDIATE STOPS ---
            intermediateStops.forEachIndexed { index, stopText ->
                TimelineInputItem(
                    label = "Stop ${index + 1}",
                    text = stopText,
                    onTextChange = { viewModel.onIntermediateQueryChange(index, it) },
                    predictions = predictions,
                    isLast = false,
                    iconColor = GoldenYellow,
                    onPredictionSelect = {
                        viewModel.onIntermediateStopSelected(index, it)
                        focusManager.clearFocus()
                    },
                    onClear = { viewModel.removeIntermediateStop(index) },
                    isRemovable = true
                )
            }

            // --- ADD STOP BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Column(
                    modifier = Modifier.width(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(BorderGray)
                    )
                }
                TextButton(
                    onClick = { viewModel.addIntermediateStop() },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NavyBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Stop", color = NavyBlue)
                }
            }

            // --- DESTINATION ---
            TimelineInputItem(
                label = "Destination",
                text = destinationText,
                onTextChange = { viewModel.onDestinationQueryChange(it) },
                predictions = predictions,
                isLast = true,
                iconColor = Color.Red,
                onPredictionSelect = {
                    viewModel.onDestinationSelected(it)
                    focusManager.clearFocus()
                },
                onClear = { viewModel.onDestinationQueryChange("") }
            )
        }
    }
}

@Composable
fun TimelineInputItem(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    predictions: List<Prediction>,
    isLast: Boolean,
    iconColor: Color,
    onPredictionSelect: (Prediction) -> Unit,
    onClear: () -> Unit,
    isRemovable: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    // Only show predictions if this specific field is focused and has text
    val showPredictions = isFocused && text.isNotEmpty() && predictions.isNotEmpty()

    // Use IntrinsicSize.Min to ensure the connector line (Left Column) matches the Input height (Right Column)
    // NOTE: Child components MUST NOT use Lazy layouts for this to work.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left Side: Icon and Line
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TripOrigin,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f) // Fill remaining height
                        .background(BorderGray)
                )
            }
        }

        // Right Side: Input
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
        ) {
            AutoSuggestTextField(
                value = text,
                onValueChange = onTextChange,
                label = label,
                isFocused = isFocused,
                onFocusChanged = { isFocused = it },
                predictions = predictions,
                showSuggestions = showPredictions,
                onPredictionSelect = onPredictionSelect,
                onClear = onClear,
                isRemovable = isRemovable
            )
        }
    }
}

@Composable
fun AutoSuggestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    predictions: List<Prediction>,
    showSuggestions: Boolean,
    onPredictionSelect: (Prediction) -> Unit,
    onClear: () -> Unit,
    isRemovable: Boolean
) {
    val focusRequester = remember { FocusRequester() }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) },
            label = { Text(label, color = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBlue,
                unfocusedBorderColor = BorderGray,
                focusedContainerColor = LightGray.copy(alpha = 0.5f),
                unfocusedContainerColor = LightGray.copy(alpha = 0.3f)
            ),
            singleLine = true,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = if (isRemovable) Icons.Default.Delete else Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = if (isRemovable) Color.Red else Color.Gray
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        // Dropdown List
        AnimatedVisibility(
            visible = showSuggestions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 200.dp),
                elevation = CardDefaults.elevatedCardElevation(6.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                // FIXED: Replaced LazyColumn with Column + verticalScroll to prevent crash with IntrinsicSize
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    predictions.forEach { prediction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPredictionSelect(prediction) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = prediction.description,
                                fontSize = 14.sp,
                                color = NavyBlue,
                                maxLines = 2
                            )
                        }
                        HorizontalDivider(color = LightGray)
                    }
                }
            }
        }
    }
}
