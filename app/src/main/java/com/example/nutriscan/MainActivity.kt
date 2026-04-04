package com.example.nutriscan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Check Firebase initialization
        val app = FirebaseApp.initializeApp(this)

        if (app == null) {
            Log.e(TAG, "FirebaseApp initialization FAILED")
            return
        } else {
            Log.d(TAG, "FirebaseApp initialized: ${app.name}")
        }

        // 2. Get Firestore instance
        val db = FirebaseFirestore.getInstance()
        Log.d(TAG, "Firestore instance created")

        // 3. Very obvious test data
        val testData = hashMapOf(
            "message" to "ANDROID TEST SUCCESS",
            "number" to 999,
            "working" to true,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d(TAG, "Attempting Firestore add...")

        db.collection("test")
            .add(testData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Firestore add SUCCESS. ID = ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore add FAILED: ${e.message}", e)
            }
    }
}