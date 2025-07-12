package com.example.smartbusai.ui.home

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartbusai.constants.Constants
import com.example.smartbusai.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    apiKey: String = Constants.PLACES_API_KEY
) {
    var isExpanded by remember { mutableStateOf(false) }
    val predictions by searchViewModel.results.collectAsState()

    Scaffold { innerPadding ->
        SearchBar(
            expanded = isExpanded,
            onExpandedChange = {
                isExpanded = it
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            windowInsets = WindowInsets(0, 0, 0, 0),
            inputField = {
                SearchBarDefaults.InputField(
                    placeholder = { Text("Select Departure Location...") },
                    query = searchViewModel.query.value,
                    onQueryChange = {
                        searchViewModel.updateQuery(it)
                        searchViewModel.searchPlaces(searchViewModel.query.value, apiKey)
                        Log.d("HomeScreen", "Query: ${searchViewModel.query.value}")
                        Log.d("HomeScreen", "Results: ${searchViewModel.results.value}")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF008800)
                        )
                    },
                    onSearch = {
                        searchViewModel.searchPlaces(searchViewModel.query.value, apiKey)
                    },
                    expanded = isExpanded,
                    onExpandedChange = {
                        isExpanded = it
                    }
                )
            },
        ) {
            LazyColumn {
                items(predictions.size) { index ->
                    val prediction = predictions[index]
                    SearchItem(
                        description = prediction.description,
                        onClick = {
                            searchViewModel.updateQuery(prediction.description)
                            searchViewModel.updateDeparture(prediction.description)
                            searchViewModel.updateDepartureStopId(prediction.place_id)
                            isExpanded = false
                            navController.navigate("routeSelection/${prediction.description}")
                        }
                    )
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
            .clickable {
                onClick()
            },
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

@Preview
@Composable
private fun SearchItemPreview() {
    SearchItem(
        "Delhi, New Delhi",
        onClick = {}
    )
}
