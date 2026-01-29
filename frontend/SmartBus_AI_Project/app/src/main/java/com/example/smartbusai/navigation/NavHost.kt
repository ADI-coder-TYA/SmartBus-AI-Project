package com.example.smartbusai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.smartbusai.ui.FeedBack.FeedbackScreen
import com.example.smartbusai.ui.home.HomeScreen
import com.example.smartbusai.ui.passengers.PassengerSelectionScreen
import com.example.smartbusai.ui.passengers.SeatLayoutScreen
import com.example.smartbusai.ui.passengers.VehicleTypeScreen
import com.example.smartbusai.ui.route.RouteSelectionScreen
import com.example.smartbusai.viewmodels.LayoutViewModel
import com.example.smartbusai.viewmodels.PassengerViewModel
import com.example.smartbusai.viewmodels.SearchViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = "main_graph"
    ) {
        // We use a nested navigation graph to scope ViewModels.
        // This ensures data is shared between screens (e.g. Passengers persist from input to summary).
        navigation(
            startDestination = startDestination,
            route = "main_graph"
        ) {
            // --- 1. Home Screen ---
            composable("home") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

                HomeScreen(
                    navController = navController,
                    searchViewModel = searchViewModel
                )
            }

            // --- 2. Route Selection ---
            composable("routeSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

                RouteSelectionScreen(
                    navController = navController,
                    searchViewModel = searchViewModel
                )
            }

            // --- 3. Passenger Entry ---
            composable("passengerSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)
                val passengerViewModel: PassengerViewModel = hiltViewModel(parentEntry)

                PassengerSelectionScreen(
                    searchViewModel = searchViewModel,
                    passengerViewModel = passengerViewModel,
                    navController = navController
                )
            }

            // --- 4. Vehicle Configuration (Rows/Cols) ---
            // Note: Route name must match what PassengerSelectionScreen calls
            composable("layout_selection_screen") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val layoutViewModel = hiltViewModel<LayoutViewModel>(parentEntry)
                val passengerViewModel = hiltViewModel<PassengerViewModel>(parentEntry)

                VehicleTypeScreen(
                    navController = navController,
                    layoutViewModel = layoutViewModel,
                    passengerViewModel = passengerViewModel
                )
            }

            // --- 5. Seat Visualizer (The Grid) ---
            // Note: Route name must match what VehicleTypeScreen calls
            composable("seat_visualizer") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val layoutViewModel = hiltViewModel<LayoutViewModel>(parentEntry)
                val passengerViewModel = hiltViewModel<PassengerViewModel>(parentEntry)

                SeatLayoutScreen(
                    navController = navController,
                    layoutViewModel = layoutViewModel,
                    passengerViewModel = passengerViewModel
                )
            }

            // --- 6. Feedback ---
            composable("feedback") {
                FeedbackScreen()
            }
        }
    }
}
