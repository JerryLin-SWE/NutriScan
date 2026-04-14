package com.example.nutriscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository()

        setContent {
            AppNavigation(authRepository = authRepository)
        }
    }
}
