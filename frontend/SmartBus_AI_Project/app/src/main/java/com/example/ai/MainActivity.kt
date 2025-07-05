package com.example.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.ui.theme.SmartBus_AI_ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartBus_AI_ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Basic(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

val stations = arrayOf("Delhi", "Bangalore", "Singrauli")

@Composable
fun Basic(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Menu(name = "Start Station")
        Spacer(modifier = Modifier.height(16.dp))
        Menu(name = "End Station")
    }
}

@Composable
fun Menu(name: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = if (selectedOption.isEmpty()) name else "$name: $selectedOption")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            stations.forEach { station ->
                DropdownMenuItem(
                    text = { Text(station) },
                    onClick = {
                        selectedOption = station
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartBus_AI_ProjectTheme {
        Basic()
    }
}
