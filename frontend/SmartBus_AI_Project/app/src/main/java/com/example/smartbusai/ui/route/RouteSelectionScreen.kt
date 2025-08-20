package com.example.smartbusai.ui.route

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbusai.R
import com.example.smartbusai.constants.Constants
import com.example.smartbusai.placesAPI.Prediction
import com.example.smartbusai.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelectionScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val predictions by searchViewModel.results.collectAsState()
    val selectedDeparture = searchViewModel.selectedDeparture.value

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.White),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column {
                // Show stored departure details for clarity
                if (selectedDeparture != null) {
                    Text(
                        text = "Departure: ${selectedDeparture.description}\nID: ${selectedDeparture.placeId}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                RouteBox(
                    predictions = predictions,
                    viewModel = searchViewModel
                )

                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors().copy(containerColor = Color(0xFF008800)),
                    shape = RectangleShape,
                    onClick = {
                        // 1. Navigate first
                        navController.navigate("passengerSelection")

                        // 2. Run location fetching in background
                        searchViewModel.fetchLatLngFromPlaceId()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Proceed")
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteBox(
    predictions: List<Prediction>,
    viewModel: SearchViewModel
) {
    var expandedDeparture by remember { mutableStateOf(false) }
    var expandedDestination by remember { mutableStateOf(false) }
    val intermediateStops = viewModel.intermediateStops
    val expansionStates = remember { mutableStateMapOf<Int, Boolean>() }

    val departure = viewModel.selectedDeparture.value?.description.orEmpty()
    var destination by viewModel.selectedDestination

    Box(
        modifier = Modifier
            .width(300.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, Color(0xFF888888), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors().copy(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(12.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        contentDescription = null,
                        painter = painterResource(R.drawable.route_icon)
                    )
                    Text("Select Route", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Departure
                SearchBar(
                    expanded = expandedDeparture,
                    onExpandedChange = { expandedDeparture = it },
                    shape = RoundedCornerShape(24.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    inputField = {
                        SearchBarDefaults.InputField(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .border(2.dp, Color(0xFF888888), RoundedCornerShape(24.dp)),
                            query = departure,
                            onQueryChange = {
                                viewModel.updateQuery(it)
                                viewModel.searchPlaces(it, Constants.PLACES_API_KEY)
                            },
                            placeholder = {
                                Text("Enter Departure Location", color = Color(0xFF888888))
                            },
                            expanded = expandedDeparture,
                            onExpandedChange = { expandedDeparture = it },
                            onSearch = {},
                            colors = TextFieldDefaults.colors().copy(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                    }
                ) {
                    LazyColumn {
                        items(predictions.size) { index ->
                            val prediction = predictions[index]
                            SearchItem(prediction.description) {
                                viewModel.updateDeparture(
                                    prediction.description,
                                    prediction.place_id
                                )
                                viewModel.updateQuery(prediction.description)
                                expandedDeparture = false
                            }
                        }
                    }
                }

                // Add Stop Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { viewModel.addIntermediateStop() }) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null)
                    }
                    Text(
                        "Add another stop...",
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF888888)
                    )
                }

                // Intermediate Stops
                intermediateStops.forEachIndexed { index, stopText ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            val isExpanded = expansionStates[index] ?: false
                            SearchBar(
                                expanded = isExpanded,
                                onExpandedChange = { expansionStates[index] = it },
                                shape = RoundedCornerShape(24.dp),
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .border(
                                                2.dp,
                                                Color(0xFF888888),
                                                RoundedCornerShape(24.dp)
                                            ),
                                        query = stopText,
                                        onQueryChange = {
                                            viewModel.updateIntermediateStop(index, it)
                                            viewModel.searchPlaces(it, Constants.PLACES_API_KEY)
                                        },
                                        placeholder = {
                                            Text(
                                                "Stop ${index + 1}",
                                                color = Color(0xFF888888)
                                            )
                                        },
                                        expanded = isExpanded,
                                        onExpandedChange = { expansionStates[index] = it },
                                        onSearch = {},
                                        colors = TextFieldDefaults.colors().copy(
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        )
                                    )
                                }
                            ) {
                                LazyColumn {
                                    items(predictions.size) { predIndex ->
                                        val prediction = predictions[predIndex]
                                        SearchItem(prediction.description) {
                                            viewModel.updateIntermediateStop(
                                                index,
                                                prediction.description
                                            )
                                            viewModel.updateIntermediateStopPlaceId(
                                                index,
                                                prediction.place_id
                                            )
                                            expansionStates[index] = false
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.removeIntermediateStop(index)
                                expansionStates.remove(index)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remove stop",
                                tint = Color.Red
                            )
                        }
                    }
                }

                // Destination
                SearchBar(
                    expanded = expandedDestination,
                    onExpandedChange = { expandedDestination = it },
                    shape = RoundedCornerShape(24.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    inputField = {
                        SearchBarDefaults.InputField(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .border(2.dp, Color(0xFF888888), RoundedCornerShape(24.dp)),
                            query = destination,
                            onQueryChange = {
                                destination = it
                                viewModel.updateQuery(it)
                                viewModel.searchPlaces(it, Constants.PLACES_API_KEY)
                            },
                            placeholder = {
                                Text("Enter Destination Location", color = Color(0xFF888888))
                            },
                            onSearch = {},
                            expanded = expandedDestination,
                            onExpandedChange = { expandedDestination = it },
                            colors = TextFieldDefaults.colors().copy(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                    }
                ) {
                    LazyColumn {
                        items(predictions.size) { index ->
                            val prediction = predictions[index]
                            SearchItem(prediction.description) {
                                viewModel.updateDestination(prediction.description)
                                viewModel.updateDestinationStopId(prediction.place_id)
                                viewModel.updateQuery(prediction.description)
                                expandedDestination = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchItem(
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFF444444)
        )
        Text(
            text = description,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
    }
}
