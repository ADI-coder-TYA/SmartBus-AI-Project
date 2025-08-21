package com.example.smartbusai.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartbusai.R
import com.example.smartbusai.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    searchViewModel: SearchViewModel,
    apiKey: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    val predictions by searchViewModel.results.collectAsState()

    val navyBlue = Color(0xFF0B1D39)
    val goldenYellow = Color(0xFFFFC107)
    val grayText = Color(0xFF888888)

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(navyBlue)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "SmartBus AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = goldenYellow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "Find the seats, tailored to your needs!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ SearchBar (still experimental)
            SearchBar(
                trailingIcon = {
                    if (searchViewModel.query.value.isNotEmpty() || isExpanded) {
                        IconButton(
                            onClick = {
                                if (searchViewModel.query.value.isNotEmpty()) {
                                    searchViewModel.updateQuery("")
                                    searchViewModel.searchPlaces("", apiKey)
                                } else {
                                    isExpanded = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = grayText
                            )
                        }
                    }
                },
                query = searchViewModel.query.value,
                onQueryChange = {
                    searchViewModel.updateQuery(it)
                    searchViewModel.searchPlaces(it, apiKey)
                },
                onSearch = {
                    searchViewModel.searchPlaces(searchViewModel.query.value, apiKey)
                },
                active = isExpanded,
                onActiveChange = { isExpanded = it },
                placeholder = { Text("Select Departure Location...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = goldenYellow
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = Color.White,
                    dividerColor = Color.LightGray
                )
            ) {
                // Suggestions inside SearchBar expanded content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // ✅ prevents overflow
                ) {
                    items(predictions.size) { index ->
                        val prediction = predictions[index]
                        SearchItem(
                            description = prediction.description,
                            onClick = {
                                searchViewModel.updateQuery(prediction.description)
                                searchViewModel.updateDeparture(
                                    description = prediction.description,
                                    placeId = prediction.place_id
                                )
                                isExpanded = false
                                navController.navigate("routeSelection")
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick Actions
            Text(
                "Quick Actions",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium.copy(color = navyBlue)
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionCard("Book a Ride", Icons.Default.ThumbUp, goldenYellow)
                ActionCard("View Routes", Icons.Default.LocationOn, Color(0xFF4DB6AC))
                ActionCard("Seat Status", Icons.Default.Search, goldenYellow)
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
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFF888888)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = description,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black
            )
        }
    }
}

@Preview
@Composable
private fun SearchItemPreview() {
    SearchItem(
        "Delhi, New Delhi",
        onClick = {}
    )
}
