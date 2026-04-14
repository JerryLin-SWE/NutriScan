package com.example.nutriscan

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch


object OnboardingRoutes {
    const val STEP_INFO = "onboarding_info"
    const val STEP_GOALS = "onboarding_goals"
    const val STEP_DIETARY = "onboarding_dietary"
}
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
                            navController.navigate("onboarding_path") {
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


        composable("dashboard") {
            DashboardScreen(onLogout = {
                authRepository.logout()
                navController.navigate("welcome") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        navigation(startDestination = OnboardingRoutes.STEP_INFO, route = "onboarding_path") {
            composable(OnboardingRoutes.STEP_INFO) { backStackEntry ->
                // Scope the ViewModel to the "onboarding_path" parent
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("onboarding_path")
                }
                val vm: OnboardingViewModel = viewModel(viewModelStoreOwner = parentEntry)

                OnboardingScreen(viewModel = vm) {
                    navController.navigate(OnboardingRoutes.STEP_GOALS)
                }
            }

            composable(OnboardingRoutes.STEP_GOALS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("onboarding_path")
                }
                val vm: OnboardingViewModel = viewModel(viewModelStoreOwner = parentEntry)

                GoalsScreen(viewModel = vm) {
                    navController.navigate(OnboardingRoutes.STEP_DIETARY) {
                        popUpTo(OnboardingRoutes.STEP_INFO) { inclusive = true }
                    }
                }


            }

            composable(OnboardingRoutes.STEP_DIETARY) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("onboarding_path")
                }
                val vm: OnboardingViewModel = viewModel(viewModelStoreOwner = parentEntry)

               DietaryScreen(viewModel = vm) {
                    navController.navigate("dashboard") {
                        popUpTo(OnboardingRoutes.STEP_INFO) { inclusive = true }
                    }
                }


            }
        }
    }
}
