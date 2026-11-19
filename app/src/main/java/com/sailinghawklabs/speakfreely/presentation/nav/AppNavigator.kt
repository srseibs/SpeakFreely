package com.sailinghawklabs.speakfreely.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sailinghawklabs.speakfreely.presentation.screen.splash.AppSplashScreen

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Splash
    ) {
        composable<Destination.Splash> {
            AppSplashScreen(
                onGotoMainScreen = {
                    navController.navigate(Destination.Main) {
                        popUpTo<Destination.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Destination.Main> {

        }

        composable<Destination.Settings> {

        }
    }
}
