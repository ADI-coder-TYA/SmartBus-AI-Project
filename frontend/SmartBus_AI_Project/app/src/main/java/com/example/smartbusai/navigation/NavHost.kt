package com.example.smartbusai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.smartbusai.constants.Constants
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
        navigation(
            startDestination = startDestination,
            route = "main_graph"
        ) {
            composable("layout") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val layoutViewModel = hiltViewModel<LayoutViewModel>(parentEntry)

                VehicleTypeScreen(layoutViewModel, { navController.navigate("seatLayout") })
            }
            composable("seatLayout") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val layoutViewModel = hiltViewModel<LayoutViewModel>(parentEntry)
                val passengerViewModel = hiltViewModel<PassengerViewModel>(parentEntry)

                SeatLayoutScreen(layoutViewModel, passengerViewModel, {
                    //navController.navigate("feedback")
                })
            }
            composable("feedback") {
                FeedbackScreen()
            }
            composable("home") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

                HomeScreen(
                    navController = navController,
                    searchViewModel = searchViewModel,
                    apiKey = Constants.PLACES_API_KEY
                )
            }

            composable("routeSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_graph")
                }
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

                RouteSelectionScreen(
                    searchViewModel = searchViewModel,
                    navController = navController
                )
            }

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
        }
    }
}
