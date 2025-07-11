package com.example.smartbusai

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
import androidx.navigation.compose.rememberNavController
import com.example.smartbusai.navigation.AppNavHost
import com.example.smartbusai.ui.home.HomeScreen
import com.example.smartbusai.ui.theme.SmartBus_AI_ProjectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartBus_AI_ProjectTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    startDestination = "home"
                )
            }
        }
    }
}
