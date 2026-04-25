package com.example.nutriscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository = AuthRepository()
        val firestoreClient = FirestoreClient()

        setContent {
            AppNavigation(authRepository = authRepository,
                firestoreClient = firestoreClient
            )

        }
    }
}
