package com.example.nutriscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository()
        val firestoreClient = FirestoreClient()

        setContent {
            AppNavigation(authRepository = authRepository,
                firestoreClient = firestoreClient
            )

        }
    }
}
