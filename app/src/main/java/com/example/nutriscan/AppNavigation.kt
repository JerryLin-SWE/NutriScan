package com.example.nutriscan

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()

    val startDestination = if (authRepository.isLoggedIn()) "dashboard" else "welcome"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(onGetStarted = { navController.navigate("login") })
        }
        composable("login") {
            LoginScreen(
                onLogin = { email, password -> /* hook up auth */ },
                onSignUp = { /* navigate to register */ },
                onForgotPassword = { /* handle forgot */ }
            )
        }
        composable("dashboard") {
            // DashboardScreen placeholder
        }
    }
}
