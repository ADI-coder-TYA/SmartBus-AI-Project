package com.example.smartbusai.ui.route

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
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
fun SearchLocationBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    predictions: List<Prediction>,
    onSelect: (Prediction) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    // Use Material3 SearchBar with explicit active/onActiveChange API
    SearchBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { /* handled by upstream if needed */ },
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon ?: {},
        trailingIcon = trailingIcon ?: {},
        colors = SearchBarDefaults.colors(containerColor = Color.White)
    ) {
        // Constrain height to avoid infinite measurement problems
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(8.dp))
        ) {
            if (predictions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text("No suggestions", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(predictions.size) { index ->
                        val p = predictions[index]
                        SearchItem(
                            description = p.description,
                            onClick = {
                                onSelect(p)
                                onActiveChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntermediateStopUI(
    index: Int,
    query: String,
    predictions: List<Prediction>,
    active: Boolean,
    onQueryChange: (String) -> Unit,
    onSelect: (Prediction) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SearchLocationBar(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = "Stop ${index + 1}",
                predictions = predictions,
                onSelect = onSelect,
                active = active,
                onActiveChange = onActiveChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF0B1D39)
                    )
                }
            )
        }

        Spacer(Modifier.width(8.dp))

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove stop", tint = Color.Red)
        }
    }
}

@Composable
fun RouteBox(
    predictions: List<Prediction>,
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    // UI state for which search field is active
    var activeDeparture by remember { mutableStateOf(false) }
    var activeDestination by remember { mutableStateOf(false) }
    val intermediateStops = viewModel.intermediateStops
    val expansionStates = remember { mutableStateMapOf<Int, Boolean>() }

    val departureText = viewModel.selectedDeparture.value?.description.orEmpty()
    var destinationText by viewModel.selectedDestination

    // Theme colors (consistent with Home)
    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val deepGreen = Color(0xFF008800)
    val lightGray = Color(0xFFEEEEEE)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(10.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.route_icon),
                        contentDescription = null,
                        tint = deepGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Build your route",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = navyBlue
                        )
                        Text(
                            "Choose departure, intermediate stops and destination",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Departure
                SectionHeader("Departure", Icons.Default.PlayArrow, goldenYellow)
                SearchLocationBar(
                    query = departureText,
                    onQueryChange = { q ->
                        viewModel.updateQuery(q)
                        viewModel.searchPlaces(q, Constants.PLACES_API_KEY)
                    },
                    placeholder = "Enter Departure Location",
                    predictions = predictions,
                    onSelect = { p ->
                        viewModel.updateDeparture(p.description, p.place_id)
                        viewModel.updateQuery(p.description)
                    },
                    active = activeDeparture,
                    onActiveChange = { activeDeparture = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = goldenYellow
                        )
                    }
                )

                // Add stop button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { viewModel.addIntermediateStop() }) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add stop",
                            tint = deepGreen
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Add another stop", color = deepGreen)
                    }
                }

                // Intermediate stops list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    intermediateStops.forEachIndexed { idx, text ->
                        val activeState = expansionStates[idx] ?: false
                        IntermediateStopUI(
                            index = idx,
                            query = text,
                            predictions = predictions,
                            active = activeState,
                            onQueryChange = { q ->
                                viewModel.updateIntermediateStop(idx, q)
                                viewModel.searchPlaces(q, Constants.PLACES_API_KEY)
                            },
                            onSelect = { p ->
                                viewModel.updateIntermediateStop(idx, p.description)
                                viewModel.updateIntermediateStopPlaceId(idx, p.place_id)
                            },
                            onActiveChange = { expansionStates[idx] = it },
                            onRemove = {
                                viewModel.removeIntermediateStop(idx)
                                expansionStates.remove(idx)
                            }
                        )
                    }
                }

                // Destination
                SectionHeader("Destination", Icons.Default.Favorite, goldenYellow)
                SearchLocationBar(
                    query = destinationText,
                    onQueryChange = { q ->
                        destinationText = q
                        viewModel.updateQuery(q)
                        viewModel.searchPlaces(q, Constants.PLACES_API_KEY)
                    },
                    placeholder = "Enter Destination Location",
                    predictions = predictions,
                    onSelect = { p ->
                        viewModel.updateDestination(p.description)
                        viewModel.updateDestinationStopId(p.place_id)
                        viewModel.updateQuery(p.description)
                    },
                    active = activeDestination,
                    onActiveChange = { activeDestination = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = goldenYellow
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelectionScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val predictions by searchViewModel.results.collectAsState()
    val selectedDeparture = searchViewModel.selectedDeparture.value

    val goldenYellow = Color(0xFFFFC107)
    val deepGreen = Color(0xFF008800)
    val lightGray = Color(0xFFEEEEEE)
    val navyBlue = Color(0xFF0B1D39)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = goldenYellow),
                title = {
                    Text(
                        "Select Your Route",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Departure summary if exists
            selectedDeparture?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = lightGray),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Selected departure:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Text(it.description, fontWeight = FontWeight.Medium, color = navyBlue)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Main route box (uses the RouteBox above)
            RouteBox(
                predictions = predictions,
                viewModel = searchViewModel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // CTA
            Button(
                onClick = {
                    // navigate then fetch lat-lngs in background — order kept so UX moves fast
                    navController.navigate("passengerSelection")
                    searchViewModel.fetchLatLngFromPlaceId()
                },
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = ButtonDefaults.buttonColors(containerColor = deepGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Proceed", color = Color.White, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
private fun SearchItem(description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFF444444)
        )
        Spacer(Modifier.width(10.dp))
        Text(text = description, modifier = Modifier.weight(1f))
    }
}
