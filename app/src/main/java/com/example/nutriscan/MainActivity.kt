package com.example.nutriscan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var user = User(
        name = "test",
        email = "test",
        age = 30
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firestoreClient = FirestoreClient()

        setContentView(R.layout.activity_main)
        @Composable
        fun StyledButtons() {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = {lifecycleScope.launch {
                    firestoreClient.insertUser(user).collect{ id->
                        user = user.copy(id = id ?: "")
                    }
                }})
                    { Text("Insert") }

                Button(onClick = {lifecycleScope.launch {
                    firestoreClient.updateUser(user).collect{ result->
                        println("FirestoreClient: is updated = $result")
                    }
                }})
                { Text("Update") }

                Button(onClick = {lifecycleScope.launch {
                    firestoreClient.getUser(user.email).collect{ result->
                        if (result != null){
                            user = result
                            println("FirestoreClient: did get user id = ${user.id}")
                            println("FirestoreClient: did get user name = ${user.name}")
                            println("FirestoreClient: did get user email = ${user.email}")
                            println("FirestoreClient: did get user age = ${user.age}")
                        }
                        else{
                            println("FirestoreClient: did not get user")
                        }
                    }
                }})
                { Text("Get") }
            }
        }

        setContent {
            StyledButtons()
        }
    }
}