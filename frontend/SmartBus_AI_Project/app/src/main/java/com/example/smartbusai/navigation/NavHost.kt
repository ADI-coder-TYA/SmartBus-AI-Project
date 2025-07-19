package com.example.smartbusai.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartbusai.constants.Constants
import com.example.smartbusai.ui.FeedBack.FeedbackScreen
import com.example.smartbusai.ui.home.HomeScreen
import com.example.smartbusai.ui.route.RouteSelectionScreen
import com.example.smartbusai.viewmodels.SearchViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                searchViewModel = hiltViewModel<SearchViewModel>(),
                apiKey = Constants.PLACES_API_KEY
            )
        }
        composable("feedback"){
            FeedbackScreen()
        }
        composable(
            route = "routeSelection/{departureLocation}",
            arguments = listOf(
                navArgument("departureLocation") {
                    type = NavType.StringType
                }
            )) {
            val searchViewModel = hiltViewModel<SearchViewModel>()
            RouteSelectionScreen(searchViewModel, navController)
        }
    }
}
