package com.sailinghawklabs.myprojecttemplate.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController, startDestination = Destination.Startup
    ) {

        composable<Destination.Startup> {
        }

        composable<Destination.ListScreen> {

        }

        composable<Destination.DetailsScreen> {

        }
    }

}

