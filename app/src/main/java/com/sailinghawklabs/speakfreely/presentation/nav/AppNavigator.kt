package com.sailinghawklabs.speakfreely.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sailinghawklabs.speakfreely.presentation.screen.main.MainScreen
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
                    navController.navigateAndDontComeBack(Destination.Main)
                }
            )
        }

        composable<Destination.Main> {
            MainScreen(
                onGotoSettings = {
                    navController.navigate(Destination.Settings)
                }
            )

        }

        composable<Destination.Settings> {

        }
    }
}
