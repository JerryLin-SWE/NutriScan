package com.example.nutriscan

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var loginError by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf("") }

    val startDestination = if (authRepository.isLoggedIn()) "dashboard" else "welcome"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("welcome") {
            WelcomeScreen(onGetStarted = { navController.navigate("login") })
        }

        composable("login") {
            LoginScreen(
                onLogin = { email, password ->
                    scope.launch {
                        val success = authRepository.login(email, password)
                        if (success) {
                            loginError = ""
                            navController.navigate("dashboard") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        } else {
                            loginError = "Invalid email or password. Please try again."
                        }
                    }
                },
                onSignUp = {
                    loginError = ""
                    navController.navigate("register")
                },
                onForgotPassword = { /* handle later */ },
                errorMessage = loginError
            )
        }

        composable("register") {
            RegisterScreen(
                onRegister = { email, password ->
                    scope.launch {
                        val success = authRepository.register(email, password)
                        if (success) {
                            registerError = ""
                            navController.navigate("onboarding") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        } else {
                            registerError = "Sign up failed. Email may already be in use."
                        }
                    }
                },
                onBackToLogin = {
                    registerError = ""
                    navController.popBackStack()
                },
                errorMessage = registerError
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onContinue = { age, metric, weight, activityLevel ->
                    // save to Firestore later
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen()
        }
    }
}
